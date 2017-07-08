(ns og.spec.users
  (:require [clojure.future :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

(def ocu-email-regex #"^[a-zA-Z0-9._%+-]+@(my\.)?okcu\.edu$")

(s/def ::id (s/and int? (comp not neg?)))
(s/def ::email (s/and string? #(first (re-matches ocu-email-regex %))))
(s/def ::password (s/and string? #(pos? (count %))))
(s/def ::role #{"student" "faculty" "admin"})
(s/def ::active? boolean?)

(s/def ::user (s/keys :req-un [::id ::email ::password ::role ::active?]))
(s/def ::users (s/coll-of ::user :distinct true))
