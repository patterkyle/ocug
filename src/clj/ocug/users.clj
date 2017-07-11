(ns ocug.users
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest] ;TODO rm?
            [clojure.spec.gen.alpha :as gen] ;TODO rm?
            [clojure.set :refer [rename-keys]]
            [com.gfredericks.test.chuck.generators :as cgen]
            [environ.core :refer [env]]
            [buddy.hashers :as hashers]
            [ocug.sql :as sql]))

;; spec
;; --------------------

(def ocu-email-regex #"[a-zA-Z0-9]+@(my\.)?okcu\.edu")
(def bcrypt-regex #"bcrypt\+sha512\$[a-f0-9]+\$[a-f0-9]+")

(s/def ::id nat-int?)
(s/def ::email (s/with-gen
                 (s/and string? (partial re-find ocu-email-regex))
                 #(cgen/string-from-regex ocu-email-regex)))
(s/def ::password (s/with-gen
                    (s/and string? (partial re-find bcrypt-regex))
                    #(cgen/string-from-regex bcrypt-regex)))
(s/def ::role #{"student" "faculty" "admin"})
(s/def ::active? boolean?)

(s/def ::user (s/keys :req-un [::id ::email ::password ::role ::active?]))
(s/def ::users (s/coll-of ::user :distinct true))

; --------------------

(s/def ::user_role ::role)
(s/def ::active ::active?)
(s/def ::db-user (s/keys
                  :req-un [::id ::email ::password ::user_role ::active]))

(s/fdef from-db
        :args (s/cat :db-user ::db-user)
        :ret ::user
        :fn #(= (vals (-> % :args :db-user)) (vals (:ret %))))

(defn from-db
  [db-user]
  (rename-keys db-user {:user_role :role :active :active?}))

; --------------------

(def db-url-regex
  #"jdbc:[a-zA-Z]+://localhost/[a-zA-Z]+\?user=[a-zA-Z0-9]+\&password=.+")

(s/def ::db-url (s/with-gen
                  (s/and string? (partial re-find db-url-regex))
                  #(cgen/string-from-regex db-url-regex)))

(s/fdef get-all
        :args (s/cat :db-url ::db-url)
        :ret ::users)

(defn get-all
  [db-url]
  (map from-db (sql/get-users db-url)))

(defn- get-by-id
  [db-url user]
  (-> (sql/get-user db-url user) first from-db))

(defn- get-by-email-and-password
  [db-url {:keys [email password]}]
  (first (filter #(and (= (:email %) email)
                       (hashers/check password (:password %)))
                 (get-all db-url))))

(defn get-one
  [db-url user]
  (cond
    (:id user) (get-by-id db-url user)
    (and (:email user)
         (:password user)) (get-by-email-and-password db-url user)))

(defn create!
  [db-url {:keys [email password] :as user}]
  (let [account-exists? (some #{email} (map :email (get-all db-url)))]
    (if-not account-exists?
      (sql/create-user db-url
                       (assoc user :password (hashers/encrypt password))))))

(defn delete! [db-url user]
  (sql/delete-user db-url user))

(defn change-password! [db-url {:keys [id password]}]
  (sql/change-password db-url {:id id :password (hashers/encrypt password)}))

(defn change-role! [db-url {:keys [id role]}]
  (sql/change-role db-url {:id id :role role}))

(defn toggle-activation! [db-url {:keys [id]}]
  (if-let [user (get-by-id db-url {:id id})]
    (sql/change-activation db-url (update user :active not))))

;; --------------------

(defn make-instruments []
  (stest/instrument `from-db)
  (stest/instrument `get-all))

(if (:debug? env)
  (make-instruments))
