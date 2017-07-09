(ns ocug.core
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.http-response :as response]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defroutes app-routes
  (GET "/" [] (response/file-response "index.html" {:root "resources/public"}))
  (route/resources "/")
  (route/not-found "The page you're looking for doesn't exist!"))

(def app (wrap-defaults app-routes api-defaults))

(defn -main [& args]
  (jetty/run-jetty app {:port 31337}))
