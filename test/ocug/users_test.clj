(ns ocug.users-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer :all]
            [clojure.test.check]
            [clojure.java.jdbc :as jdbc]
            [migratus.core :as migratus]
            [environ.core :refer [env]]
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

(s/def ::unencrypted-password (s/and string? (partial re-find #".+")))

(defn emails-and-passwords
  ([n]
   (for [email (gen/sample (s/gen :ocug.users/email) n)
         password (gen/sample (s/gen ::unencrypted-password) n)]
     {:email email :password password}))
  ([] (emails-and-passwords 20)))

(def user-credentials (atom '()))

(defn- setup []
  (if (pos? (Integer/parseInt (re-find #"\d+"
                                       (migratus/pending-list test-db-conn))))
    (migratus/migrate test-db-conn))
  (let [ocug-user {:email "ocug-user@okcu.edu" :password "ocugpwd"}]
    (reset! user-credentials (conj (emails-and-passwords) ocug-user))
    (doseq [user @user-credentials]
      (users/create! test-db-url user))))

(defn- reset []
  (migratus/reset test-db-conn)
  (reset! test-users '()))

(use-fixtures :once
  (fn [f] (setup) (f) (reset)))

(declare ^:dynamic *txn*)

(use-fixtures :each
  (fn [f]
    (jdbc/with-db-transaction [transaction test-db-url]
      (jdbc/db-set-rollback-only! transaction)
      (binding [*txn* transaction] (f)))))

(if (:debug? env)
  (users/make-instruments))

;; tests
;; --------------------

(deftest from-db-test
  (is (spec-compliant? `users/from-db)))

(deftest get-all-test
  (let [res (users/get-all test-db-url)]
    (is (not (nil? res)))
    (is (s/valid? (keyword `users/users) res))))
