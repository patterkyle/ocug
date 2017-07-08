(ns og.users
  (:require [clojure.set :refer [rename-keys]]
            [og.sql :as sql]
            [buddy.hashers :as hashers]))

(defn- from-db
  [user]
  (rename-keys user {:user_role :role :active :active?}))

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
