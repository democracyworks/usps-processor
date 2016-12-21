(ns usps-processor.queue
  (:require [langohr.core :as rmq]
            [kehaar.configured :as kehaar]
            [kehaar.rabbitmq]
            [turbovote.resource-config :refer [config]]))

(defn initialize
  [rabbit-config]
  (let [max-connection-attempts 5
        connection (kehaar.rabbitmq/connect-with-retries
                    rabbit-config max-connection-attempts)
        kehaar-resources (kehaar/init! connection
                                       (config [:rabbitmq :kehaar]))]
    {:connections [connection]
     :kehaar-resources kehaar-resources}))

(defn close-resources! [resources]
  (doseq [resource resources]
    (when-not (rmq/closed? resource) (rmq/close resource))))

(defn close-all! [{:keys [connections kehaar-resources]}]
  (kehaar/shutdown! kehaar-resources)
  (close-resources! connections))
