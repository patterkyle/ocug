(ns ocug.users-test
  (:require [clojure.future :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen] ;TODO remove me?
            [clojure.test :refer :all]
            [ocug.users :as users]))

(users/make-instruments)

;; utils
;; --------------------

(defn passes-spec-check?
  [namespaced-fn]
  (let [res (stest/summarize-results (stest/check namespaced-fn))]
    (and (not (or (contains? res :check-failed)
                  (contains? res :check-threw)))
         (= (:check-passed res) 1))))

;; --------------------

(deftest from-db-test
  (is (passes-spec-check? `users/from-db)))
