(ns og.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [:h3 "hello"]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))
