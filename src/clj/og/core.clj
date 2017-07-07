(ns og.core
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.util.http-response :as response]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defroutes site-routes
  (GET "/" [] (response/file-response "index.html" {:root "resources/public"}))
  (route/resources "/")
  (route/not-found "not found"))

(defn -main [& args]
  (jetty/run-jetty site-routes {:port 1337}))
