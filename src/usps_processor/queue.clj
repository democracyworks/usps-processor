(ns usps-processor.queue
  (:require [langohr.core :as rmq]
            [kehaar.wire-up :as wire-up]
            [kehaar.rabbitmq]
            [turbovote.resource-config :refer [config]]
            [usps-processor.channels :as channels]))

(defn initialize
  [rabbit-config]
  (let [max-connection-attempts 5
        connection (kehaar.rabbitmq/connect-with-retries
                    rabbit-config max-connection-attempts)]
    (let [exchanges [(wire-up/declare-events-exchange
                      connection
                      "events" "topic"
                      (config [:rabbitmq :topics "events"]))]
          incoming-events []
          incoming-services []
          external-services []
          outgoing-events [(wire-up/outgoing-events-channel
                            connection
                            "events"
                            "usps-scans"
                            channels/usps-scans-out)]]
      {:connections [connection]
       :channels (vec (concat exchanges
                              incoming-events
                              incoming-services
                              external-services
                              outgoing-events))})))

(defn close-resources! [resources]
  (doseq [resource resources]
    (when-not (rmq/closed? resource) (rmq/close resource))))

(defn close-all! [{:keys [connections channels]}]
  (close-resources! channels)
  (close-resources! connections))
