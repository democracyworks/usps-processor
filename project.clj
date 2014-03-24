(defproject usps-processor "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [turbovote.resource-config "0.1.1"]
                 [turbovote.datomic-toolbox "0.1.0-SNAPSHOT"]
                 [com.datomic/datomic-pro "0.9.4609" :exclusions [org.slf4j/slf4j-nop]]
                 [clj-aws-s3 "0.3.8"]
                 [com.cemerick/bandalore "0.0.5"]
                 [org.clojure/data.csv "0.1.2"]
                 [turbovote.imbarcode "0.1.4-SNAPSHOT"]]
  :main usps-processor.core)
