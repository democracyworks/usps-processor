(defproject usps-processor "0.1.0-SNAPSHOT"
  :description "USPS IMb scan processor"
  :url "https://github.com/democracyworks/usps-processor"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.391"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [turbovote.resource-config "0.2.0"]
                 [democracyworks/datomic-toolbox "2.0.2"
                  :exclusions [com.datomic/datomic-pro]]
                 [com.datomic/datomic-pro "0.9.5394"
                  :exclusions [org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12
                               commons-codec
                               org.jboss.logging/jboss-logging]]
                 [com.amazonaws/aws-java-sdk "1.11.6"
                  :exclusions [commons-codec commons-logging
                               com.fasterxml.jackson.dataformat/jackson-dataformat-cbor
                               com.fasterxml.jackson.core/jackson-core]]
                 [clj-aws-s3 "0.3.10"
                  :exclusions [commons-codec commons-logging
                               com.fasterxml.jackson.core/jackson-core
                               com.fasterxml.jackson.core/jackson-annotations
                               com.amazonaws/aws-java-sdk]]
                 [clj-time "0.12.0"]
                 [democracyworks/squishy "3.0.1"
                  :exclusions [commons-codec]]
                 [org.clojure/data.csv "0.1.3"]
                 [turbovote.imbarcode "0.1.6"
                  :exclusions [org.clojure/clojure
                               org.clojure/tools.reader]]
                 [democracyworks/kehaar "0.6.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.immutant/web "2.1.5"
                  :exclusions [commons-codec]]
                 [riemann-clojure-client "0.4.2"]
                 [com.novemberain/langohr "3.6.1"]
                 [prismatic/schema "1.1.3"]]
  :plugins [[lein-immutant "2.1.0"]]
  :main ^:skip-aot usps-processor.core
  :target-path "target/%s"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username :env
                                   :password :env}}
  :uberjar-name "usps-processor.jar"
  :profiles {:uberjar {:aot :all}
             :dev-common {:resource-paths ["dev-resources"]
                          :source-paths ["dev-src"]}
             :dev-overrides {}
             :dev [:dev-common :dev-overrides]
             :test {:resource-paths ["test-resources"]
                    :main usps-processor.core-test
                    :jvm-opts ["-Dlog-level=OFF"]}})
