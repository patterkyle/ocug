(ns og.core
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :as defaults]
            [ring.util.http-response :as response]
            [ring.adapter.jetty :as jetty]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defroutes app-routes
  (GET "/" [] (response/file-response "index.html" {:root "resources/public"}))
  (route/resources "/")
  (route/not-found "The page you're looking for doesn't exist!"))

(def app (defaults/wrap-defaults app-routes defaults/api-defaults))

(defn -main [& args]
  (jetty/run-jetty app {:port 3449}))
