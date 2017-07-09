(ns ocug.users-test
  (:require [clojure.future :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer :all]
            [ocug.users :as users]))

;; utils
;; --------------------

;; (defn passes-spec-check?
;;   [namespaced-fn]
;;   (let [res (stest/summarize-results (stest/check namespaced-fn))]
;;     (and (not (or (contains? res :check-failed)
;;                   (contains? res :check-threw)))
;;          (= (:check-passed res) 1))))

(defn spec-compliant?
  [namespaced-fn]
  (let [{:keys [check-passed check-failed check-threw]}
        (stest/summarize-results (stest/check namespaced-fn))]
    (boolean (or (or check-failed check-threw)
                 #(= (:check-passed %) 1)))))

;; (defn create-user-db []
;;   (let [users (gen/generate (s/gen (s/and :ocug.users/users
;;                                           #(-> % count pos?))))]
;;     users))

;; setup
;; --------------------

;; (users/make-instruments)

;; tests
;; --------------------

;; (deftest from-db-test
;;   (is true))

(deftest from-db-test
  (is (stest/check `users/from-db))
  ;; (is (spec-compliant? `users/from-db))
  )

;; (deftest from-db-test
;;   (is (stest/check `users/from-db)))
