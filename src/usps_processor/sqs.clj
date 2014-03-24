(ns usps-processor.sqs
  (:require [usps-processor.data-readers]
            [cemerick.bandalore :as sqs]
            [turbovote.resource-config :refer [config]]))

(def sqs-client
  (doto (sqs/create-client (config :aws :creds :access-key)
                           (config :aws :creds :secret-key))
    (.setRegion (config :aws :sqs :region))))

(defn consume-messages
  [f]
  (let [q (config :aws :sqs :queue)]
    (future
      (dorun
       (map (sqs/deleting-consumer sqs-client (comp f :body))
            (sqs/polling-receive sqs-client q :max-wait Long/MAX_VALUE :limit 10))))))
