(defproject usps-processor "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.slf4j/slf4j-simple "1.7.6"]
                 [turbovote.resource-config "0.1.1"]
                 [turbovote.datomic-toolbox "0.1.0-SNAPSHOT" :exclusions [com.datomic/datomic-pro]]
                 [com.datomic/datomic-pro "0.9.4699" :exclusions [org.slf4j/slf4j-nop]]
                 [clj-aws-s3 "0.3.8"]
                 [com.cemerick/bandalore "0.0.5"]
                 [org.clojure/data.csv "0.1.2"]
                 [turbovote.imbarcode "0.1.4-SNAPSHOT"]
                 [compojure "1.1.6"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.1.16"]]
  :plugins [[lein-immutant "1.2.0"]]
  :profiles {:uberjar {:aot [usps-processor.importer
                             usps-processor.api]}
             :production {:resource-paths ["envs/production/resources"]}}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username :env
                                   :password :env}}
  :ring {:handler usps-processor.api/app}
  :uberjar-name "usps-processor.jar")
