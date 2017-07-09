(ns ocug.sql
  (:require [hugsql.core :as hugsql]))

(defn def-sql-fns [sql-files]
  (let [path "sql/"]
    (doseq [file sql-files]
      (hugsql/def-db-fns (str path file)))))

(def sql-files ["users.sql"])

(def-sql-fns sql-files)
