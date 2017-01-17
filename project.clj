(defproject usps-processor "0.1.0-SNAPSHOT"
  :description "USPS IMb scan processor"
  :url "https://github.com/democracyworks/usps-processor"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.395"]
                 [ch.qos.logback/logback-classic "1.1.8"]
                 [turbovote.resource-config "0.2.1"]
                 [democracyworks/datomic-toolbox "2.0.4"
                  :exclusions [com.datomic/datomic-pro]]
                 [com.datomic/datomic-pro "0.9.5544"
                  :exclusions [org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12
                               commons-codec
                               org.jboss.logging/jboss-logging
                               org.apache.httpcomponents/httpcore]]
                 [com.amazonaws/aws-java-sdk "1.11.6"
                  :exclusions [commons-codec commons-logging
                               com.fasterxml.jackson.dataformat/jackson-dataformat-cbor
                               com.fasterxml.jackson.core/jackson-core]]
                 [clj-aws-s3 "0.3.10"
                  :exclusions [commons-codec commons-logging
                               com.fasterxml.jackson.core/jackson-core
                               com.fasterxml.jackson.core/jackson-annotations
                               com.amazonaws/aws-java-sdk]]
                 [clj-time "0.13.0"]
                 [democracyworks/squishy "3.0.1"
                  :exclusions [commons-codec
                               com.amazonaws/aws-java-sdk-sqs]]
                 [org.clojure/data.csv "0.1.3"]
                 [turbovote.imbarcode "0.1.6"
                  :exclusions [org.clojure/clojure
                               org.clojure/tools.reader
                               org.clojure/clojurescript]]
                 [democracyworks/kehaar "0.8.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.immutant/web "2.1.6"
                  :exclusions [commons-codec commons-io]]
                 [com.novemberain/langohr "3.7.0"]
                 [prismatic/schema "1.1.3"]
                 [democracyworks/phantom-zone "0.1.1"]]
  :plugins [[lein-immutant "2.1.0"]
            [com.carouselapps/jar-copier "0.3.0"]]
  :java-agents [[com.newrelic.agent.java/newrelic-agent "3.35.1"]]
  :jar-copier {:java-agents true
               :destination "resources/jars"}
  :prep-tasks ["javac" "compile" "jar-copier"]
  :main ^:skip-aot usps-processor.core
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username :env/datomic_username
                                   :password :env/datomic_password}}
  :uberjar-name "usps-processor.jar"
  :profiles {:uberjar {:aot :all}
             :dev-common {:resource-paths ["dev-resources"]
                          :source-paths ["dev-src"]}
             :dev-overrides {}
             :dev [:dev-common :dev-overrides]
             :production {:jvm-opts ["-XX:+UseG1GC"]}
             :test {:resource-paths ["test-resources"]
                    :main usps-processor.core-test
                    :jvm-opts ["-Dlog-level=OFF"]}})
