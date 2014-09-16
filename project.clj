(defproject pool "0.3.0-SNAPSHOT"
  :description "Pooling Library for Clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/kul/pool"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.apache.commons/commons-pool2 "2.2"]])
