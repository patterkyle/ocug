(defproject ocug "0.1.0-SNAPSHOT"
  :description "TODO: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.671" :scope "provided"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [com.gfredericks/test.chuck "0.2.7"]
                 [ring/ring-core "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [ring/ring-jetty-adapter "1.6.1"]
                 [metosin/ring-http-response "0.9.0"]
                 [environ "1.1.0"]
                 [compojure "1.6.0"]
                 [buddy "1.3.0"]
                 [org.postgresql/postgresql "42.1.1"]
                 [com.layerware/hugsql "0.4.7"]
                 [com.taoensso/timbre "4.10.0"]
                 [migratus "0.9.7"]
                 [reagent "0.7.0"]
                 [secretary "1.2.3"]
                 [re-frame "0.9.4"]]

  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"
                                    "figwheel_server.log"]

  :plugins [[lein-cljsbuild "1.1.7-SNAPSHOT"]
            [lein-environ "1.1.0"]
            [migratus-lein "0.5.0"]]

  :profiles {:dev-local {:dependencies [[figwheel-sidecar "0.5.11"]
                                        [com.cemerick/piggieback "0.2.1"]]
                         :plugins      [[lein-figwheel "0.5.11"]]}

             :dev-env-vars {}
             :test-env-vars {}

             :dev [:dev-local :dev-env-vars]
             :test [:test-env-vars]

             :uberjar {:aot :all}}

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler ocug.core/app}

  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "ocug.core/mount-root"}
     :compiler     {:main                 ocug.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config
                                           {:features-to-install :all}}}}
    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            ocug.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.debug false}
                    :pretty-print    false}}]}

  :target-path "target/%s"
  :main ^:skip-aot ocug.core)
