(ns og.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [og.views :as views]
            [og.routes :as routes]
            [og.subs]
            [og.events]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:export init []
  (routes/make-app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
