(ns usps-processor.core
  (:require [usps-processor.importer :as importer]
            [usps-processor.queue :as queue]
            [usps-processor.channels :as channels]
            [datomic-toolbox.core :as dtc]
            [squishy.core :as sqs]
            [immutant.util :as immutant]
            [turbovote.resource-config :refer [config]]
            [clojure.tools.logging :as log])
  (:gen-class))

(defonce consumer-id (atom nil))

(defn start-importer []
  (swap! consumer-id (fn [cid]
                       (when cid (sqs/stop-consumer cid))
                       (importer/start-message-consumer))))


(defn stop-importer []
  (swap! consumer-id (fn [cid]
                         (when cid (sqs/stop-consumer cid))
                         nil)))

(defn -main [& args]
  (log/info "Starting up...")
  (cond (config [:datomic :initialize]) (dtc/initialize (config [:datomic]))
        (config [:datomic :run-migrations]) (do (dtc/configure! (config [:datomic]))
                                                (dtc/run-migrations)))
  (log/info "Datomic initialized")
  (let [rabbit-resources (queue/initialize (config [:rabbitmq :connection]))]
    (log/info "RabbitMQ initialized")
    (start-importer)
    (log/info "Importer started")
    (immutant/at-exit (fn []
                        (queue/close-all! rabbit-resources)
                        (channels/close-all!)
                        (stop-importer)))))
