(ns ocug.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [ocug.views :as views]
            [ocug.routes :as routes]
            [ocug.subs]
            [ocug.events]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:export init []
  (routes/make-app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
