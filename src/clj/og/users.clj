(ns og.users
  (:require [og.db :refer [db]]
            [og.sql :as sql]
            [buddy.hashers :as hashers]))

(defn get-all [db]
  (sql/get-users db))

(defn get-by-id [db user]
  (first (sql/get-user db user)))

(defn create! [db {:keys [password] :as user}]
  (sql/create-user db (assoc user :password (hashers/encrypt password))))

(defn delete! [db user]
  (sql/delete-user db user))

(defn change-password! [db {:keys [id password]}]
  (sql/change-password db {:id id :password (hashers/encrypt password)}))

(defn change-role! [db {:keys [id new-role]}]
  (sql/change-role db {:id id :role new-role}))

(defn toggle-activation! [db {:keys [id]}]
  (if-let [user (get-by-id db {:id id})]
    (sql/change-activation db (update user :active not))))
