(defproject plane "0.1.0"
  :description "Get the plane of a given point"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main plane.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})