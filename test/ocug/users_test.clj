(ns ocug.users-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer :all]
            [clojure.test.check]
            [clojure.java.jdbc :as jdbc]
            [migratus.core :as migratus]
            [environ.core :refer [env]]
            [buddy.hashers :as hashers]
            [ocug.users :as users]))

;; setup
;; --------------------

(defn spec-compliant?
  [namespaced-fn]
  (let [{:keys [check-passed check-failed check-threw]}
        (stest/summarize-results (stest/check namespaced-fn))]
    (and (not (or check-failed check-threw))
         (= check-passed 1))))

(def test-db-url
  "jdbc:postgresql://localhost/ocugt?user=ocugt&password=ocugtpwd")

(def test-db-conn
  {:store :database :migration-dir "migrations" :db test-db-url})

(defn gen-user-credentials
  ([n]
   (for [email (gen/sample (s/gen (keyword `users/email)) n)
         password (gen/sample (s/gen (keyword :unencrypted/password)) n)]
     {:email email :password password}))
  ([] (gen-user-credentials 20)))

(def user-credentials (atom '()))

(def test-user-credentials
  {:email "user@my.okcu.edu" :password "userpwd"})

(defn user? [user]
  (s/valid? (keyword `users/user) user))

(defn- setup []
  (if (pos? (Integer/parseInt (re-find #"\d+"
                                       (migratus/pending-list test-db-conn))))
    (migratus/migrate test-db-conn))
  (reset! user-credentials (conj (gen-user-credentials) test-user-credentials))
  (doseq [user @user-credentials]
    (users/create! test-db-url user)))

(defn- reset []
  (migratus/reset test-db-conn)
  (reset! user-credentials '()))

(use-fixtures :once
  (fn [f] (setup) (f) (reset)))

(declare ^:dynamic *txn*)

(use-fixtures :each
  (fn [f]
    (jdbc/with-db-transaction [transaction test-db-url]
      (jdbc/db-set-rollback-only! transaction)
      (binding [*txn* transaction] (f)))))

(users/make-instruments)

;; tests
;; --------------------

(deftest from-db-test
  (is (spec-compliant? `users/from-db)))

(deftest get-all-test
  (let [res (users/get-all test-db-url)]
    (is (some? res))
    (is (every? user? res))))

(deftest get-by-id-test
  (let [res (users/get-by-id test-db-url {:id 1})]
    (is (some? res))
    (is (user? res))))

(deftest get-by-email-and-password-test
  (let [res (users/get-by-email-and-password test-db-url test-user-credentials)]
    (is (some? res))
    (is (user? res))))

(deftest get-one-test
  (let [res (users/get-one test-db-url test-user-credentials)]
    (is (some? res))
    (is (user? res))))

(deftest create!-test
  (is (nil? (users/create! test-db-url test-user-credentials))) ;already exists
  (let [new-user (users/create! test-db-url {:email "newuser@okcu.edu"
                                             :password "newuserpwd"})]
    (is (some? new-user))
    (is (user? new-user))))

(deftest delete!-test
  (let [u (users/create! test-db-url {:email "deletetest@okcu.edu"
                                      :password "deletetestpwd"})]
    (is (users/delete! test-db-url u))))

(deftest change-password!-test
  (let [u (users/create! test-db-url {:email "u@my.okcu.edu"
                                      :password "upwd"})]
    (is (hashers/check "upwd" (:password u)))
    (is (users/change-password! test-db-url
                                (assoc u :new-password "newupwd")))
    (is (= (dissoc u :password)
           (dissoc (users/change-password! test-db-url
                                           (assoc u :new-password "upwd"))
                   :password)))))

(deftest change-role!-test
  (let [test-user #(users/get-one test-db-url test-user-credentials)]
    (is (= "student" (:role (test-user))))
    (is (user? (users/change-role! test-db-url
                                   (assoc (test-user) :new-role "admin"))))
    (is (= "admin" (:role (users/get-one test-db-url (test-user)))))))

(deftest toggle-activation!-test
  (let [test-user #(users/get-one test-db-url test-user-credentials)]
    (is (:active? (test-user)))
    (is (user? (users/toggle-activation! test-db-url (test-user))))
    (is (not (:active? (test-user))))))
