(ns ocug.users-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer :all]
            [clojure.test.check]
            [ocug.users :as users]))

;; utils
;; --------------------

(defn spec-compliant?
  [namespaced-fn]
  (let [{:keys [check-passed check-failed check-threw]}
        (stest/summarize-results (stest/check namespaced-fn))]
    (and (not (or check-failed check-threw))
         (= check-passed 1))))

;; (defn create-user-db []
;;   (let [users (gen/generate (s/gen (s/and :ocug.users/users
;;                                           #(-> % count pos?))))]
;;     users))

;; setup
;; --------------------

(users/make-instruments)

;; tests
;; --------------------

(deftest from-db-test
  (is (spec-compliant? `users/from-db)))
