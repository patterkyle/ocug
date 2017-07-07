(ns og.views
  (:require [re-frame.core :as re-frame]))

(defn home-panel []
  (let [app-name (re-frame/subscribe [:app-name])]
    (fn []
      [:div.container
       [:h3 (str "Welcome to " @app-name ".")]
       ])))

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
