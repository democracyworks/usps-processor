(defproject usps-processor "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.385"]
                 [org.slf4j/slf4j-simple "1.7.21"]
                 [turbovote.resource-config "0.2.0"]
                 [democracyworks/datomic-toolbox "2.0.1"
                  :exclusions [com.datomic/datomic-pro]]
                 [com.datomic/datomic-pro "0.9.5385"
                  :exclusions [org.slf4j/slf4j-nop commons-codec
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
                 [democracyworks/squishy "2.0.0"
                  :exclusions [commons-codec]]
                 [org.clojure/data.csv "0.1.3"]
                 [turbovote.imbarcode "0.1.5"
                  :exclusions [org.clojure/clojure
                               org.clojure/tools.reader]]
                 [democracyworks/kehaar "0.5.0"]
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
                    :main usps-processor.core-test}})
