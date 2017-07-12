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
(s/def :unencrypted/password (s/and string? (partial re-find #".+")))
(s/def :encrypted/password (s/with-gen
                             (s/and string? (partial re-find bcrypt-regex))
                             #(cgen/string-from-regex bcrypt-regex)))
(s/def ::role #{"student" "faculty" "admin"})
(s/def ::active? boolean?)

(s/def ::user-credentials
  (s/or :email-and-password (s/keys :req-un [::email :unencrypted/password])
        :id (s/keys :req-un [::id])))

(s/def ::user (s/keys :req-un [::id ::email :encrypted/password
                               ::role ::active?]))
(s/def ::users (s/coll-of ::user :distinct true))

; --------------------

(s/def ::user_role ::role)
(s/def ::active ::active?)
(s/def ::db-user (s/keys
                  :req-un [::id ::email :encrypted/password
                           ::user_role ::active]))

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

(s/fdef get-by-id
        :args (s/cat :db-url ::db-url
                     :user (s/spec (s/keys :req-un [::id])))
        :ret (s/nilable ::user))

(defn get-by-id
  [db-url user]
  (-> (sql/get-user db-url user) first from-db))

(s/fdef get-by-email-and-password
        :args (s/cat :db ::db-url
                     :user ::user-credentials)
        :ret (s/nilable ::user))

(defn get-by-email-and-password
  [db-url {:keys [email password]}]
  (first (filter #(and (= (:email %) email)
                       (hashers/check password (:password %)))
                 (get-all db-url))))

(s/fdef get-one
        :args (s/cat :db ::db-url
                     :user ::user-credentials)
        :ret (s/nilable ::user))

(defn get-one
  [db-url user]
  (cond
    (:id user) (get-by-id db-url user)
    (and (:email user)
         (:password user)) (get-by-email-and-password db-url user)))

(s/fdef create!
        :args (s/cat :db ::db-url
                     :user ::user-credentials)
        :ret (s/nilable ::user))

(defn create!
  [db-url {:keys [email password] :as user}]
  (if-not (some #{email} (map :email (get-all db-url)))
    (get-one db-url
             (sql/create-user db-url (assoc user :password
                                            (hashers/encrypt password))))))

(s/fdef delete!
        :args (s/cat :db ::db-url
                     :user ::user-credentials)
        :ret boolean?)

(defn delete! [db-url user]
  (if-let [u (get-one db-url user)]
    (= 1 (sql/delete-user db-url user))
    false))

(s/def ::new-password :unencrypted/password)

(s/fdef change-password!
        :args (s/cat :db ::db-url
                     :user (s/spec (s/keys :req-un [::id ::new-password])))
        :ret (s/nilable ::user))

(defn change-password! [db-url {:keys [id new-password] :as user}]
  (if-let [u (get-one db-url user)]
    (get-one
     db-url
     (sql/change-password
      db-url
      {:id id :new-password (hashers/encrypt new-password)}))))

(s/fdef change-role!
        :args (s/cat :db ::db-url
                     :user (s/spec (s/keys :req-un [::id ::new-role])))
        :ret (s/nilable ::user))

(defn change-role! [db-url {:keys [id new-role] :as user}]
  (if-let [u (get-one db-url user)]
    (get-one db-url (sql/change-role db-url user))))

(s/fdef toggle-activation!
        :args (s/cat :db ::db-url
                     :user (s/spec (s/keys :req-un [::id])))
        :ret (s/nilable ::user))

(defn toggle-activation! [db-url {:keys [id] :as user}]
  (if-let [u (get-one db-url user)]
    (get-one db-url
             (sql/change-activation db-url (update user :active? not)))))

;; --------------------

(defn make-instruments []
  (let [fns [`from-db `get-all `get-by-id `get-by-email-and-password `get-one
             `create! `delete!]]
    (for [f fns]
      (stest/instrument f))))

(if (:debug? env)
  (make-instruments))
