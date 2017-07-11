(ns ocug.db
  (:require [environ.core :refer [env]]))

(def db-url (env :database-url))
