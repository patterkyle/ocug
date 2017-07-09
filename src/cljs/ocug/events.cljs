(ns ocug.events
  (:require [re-frame.core :as re-frame]
            [ocug.db :as db]))

;; interceptors
;; --------------------

;; (defn check-and-throw
;;   [db-spec db]
;;   (when-not (spec/valid? db-spec db)
;;     (throw
;;      (ex-info (str "spec check failed: " (spec/explain-str db-spec db)) {}))))

;; (def check-spec (re-frame/after (partial check-and-throw :og.db/db)))
(def app-interceptors [])

;; init
;; --------------------

(re-frame/reg-event-db
 :initialize-db
 app-interceptors
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 app-interceptors
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))
