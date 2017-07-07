(defproject og "0.1.0-SNAPSHOT"
  :description "TODO: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89" :scope "provided"]
                 [mount "0.1.11"]
                 [buddy "1.3.0"]
                 [ring/ring-core "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [ring/ring-jetty-adapter "1.6.1"]
                 [compojure "1.6.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [metosin/compojure-api "1.1.10"]
                 [org.postgresql/postgresql "42.1.1"]
                 [com.layerware/hugsql "0.4.7"]
                 [com.taoensso/timbre "4.10.0"]
                 [migratus "0.9.7"]
                 [reagent "0.7.0"]
                 [secretary "1.2.3"]
                 [re-frame "0.9.4"]]

  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :plugins [[lein-cljsbuild "1.1.5"]
            [migratus-lein "0.5.0"]]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler og.core/app}

  :migratus {:store :database
             :migration-dir "migrations"
             ;; :db ~(get (system/getenv) "database_url")
             :db {:classname "com.postgresql.driver"
                  :subprotocol "postgresql"
                  :subname "//localhost:5432/og"
                  :user "ogmin"
                  :password "ogminpwd"}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "og.core/mount-root"}
     :compiler     {:main                 og.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config
                                           {:features-to-install :all}}}}
    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            og.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.debug false}
                    :pretty-print    false}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.9"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :plugins      [[lein-figwheel "0.5.9"]]}
             :uberjar {:aot :all}}

  :target-path "target/%s"
  :main ^:skip-aot og.core)
