(defproject maze "0.1.0-SNAPSHOT"
  :description "https://challenge.flipboard.com/"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "2.0.1"]
                 [http-kit "2.1.19"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.2.374"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot maze.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
