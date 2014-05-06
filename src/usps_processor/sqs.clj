(ns usps-processor.sqs
  (:require [usps-processor.data-readers]
            [cemerick.bandalore :as sqs]
            [turbovote.resource-config :refer [config]]
            [clojure.tools.logging :refer [error]]))

(defn client []
  (doto (sqs/create-client (config :aws :creds :access-key)
                           (config :aws :creds :secret-key))
    (.setRegion (config :aws :sqs :region))))

(defn- create-queue [queue-key]
  (sqs/create-queue (client) (config :aws :sqs queue-key)))

(def ^:private memoized-create-queue (memoize create-queue))

(defn- get-queue [] (memoized-create-queue :queue))

(defn- get-fail-queue [] (memoized-create-queue :fail-queue))

(defn report-error [client body error]
  (let [q (get-fail-queue)]
    (sqs/send client q (pr-str {:body body :error error}))))

(defn safe-process [client f]
  (fn [message]
    (try (f message)
      (catch Exception e
        (let [body (:body message)]
          (error "Failed to process" body e)
          (report-error client body e))))))

(defn consume-messages
  [client f]
  (let [q (get-queue)]
    (future
      (dorun
       (map (sqs/deleting-consumer client (safe-process client f))
            (sqs/polling-receive client q :max-wait Long/MAX_VALUE :limit 10))))))
