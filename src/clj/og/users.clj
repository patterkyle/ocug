(ns og.users
  (:require [clojure.future :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen] ;TODO remove me?
            [com.gfredericks.test.chuck.generators :as cgen]
            [clojure.set :refer [rename-keys]]
            [buddy.hashers :as hashers]
            [og.config :as config]
            [og.sql :as sql]))

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

;; --------------------

(s/def ::user_role ::role)
(s/def ::active ::active?)

(s/fdef from-db
        :args (s/cat :db-user
                     (s/keys
                      :req-un [::id ::email ::password ::user_role ::active]))
        :ret ::user
        :fn #(= (vals (-> % :args :db-user)) (vals (:ret %))))

(defn from-db
  [db-user]
  (rename-keys db-user {:user_role :role :active :active?}))

(defn get-all
  [db]
  (map from-db (sql/get-users db)))

(defn- get-by-id
  [db user]
  (-> (sql/get-user db user) first from-db))

(defn- get-by-email-and-password
  [db {:keys [email password]}]
  (first (filter #(and (= (:email %) email)
                       (hashers/check password (:password %)))
                 (get-all db))))

(defn get-one
  [db user]
  (cond
    (:id user) (get-by-id db user)
    (and (:email user)
         (:password user)) (get-by-email-and-password db user)))

(defn create!
  [db {:keys [email password] :as user}]
  (let [account-exists? (some #{email} (map :email (get-all db)))]
    (if-not account-exists?
      (sql/create-user db (assoc user :password (hashers/encrypt password))))))

(defn delete! [db user]
  (sql/delete-user db user))

(defn change-password! [db {:keys [id password]}]
  (sql/change-password db {:id id :password (hashers/encrypt password)}))

(defn change-role! [db {:keys [id role]}]
  (sql/change-role db {:id id :role role}))

(defn toggle-activation! [db {:keys [id]}]
  (if-let [user (get-by-id db {:id id})]
    (sql/change-activation db (update user :active not))))

;; --------------------

(defn make-instruments []
  (stest/instrument `from-db))

(if config/debug?
  (make-instruments))
