(ns ocug.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :app-name
 (fn [db]
   (:app-name db)))

(re-frame/reg-sub
 :active-panel
 (fn [db]
   (:active-panel db)))
