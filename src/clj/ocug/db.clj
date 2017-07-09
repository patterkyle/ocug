(ns ocug.db
  (:require [environ.core :refer [env]]))

(def db (env :database-url))
