(ns usps-processor.importer
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [usps-processor.queue :as queue]
            [squishy.core :as sqs]
            [clojure.tools.logging :as log]
            [turbovote.resource-config :refer [config]]
            [datomic-toolbox.core :as dt]
            [clojure.edn :as edn])
  (:gen-class))

(defn process-file [message]
  (let [{:keys [bucket filename]} (edn/read-string (:body message))]
    (log/debug "Processing" bucket "/" filename)
    (let [scans (parse/parse (s3/reader-from-s3 bucket filename))
          scan-count (count scans)
          stored-scans (db/store-scans scans)]
      (queue/publish-scans stored-scans)
      (log/debug "Processed" scan-count "scans from" bucket "/" filename))))

(defn -main [& args]
  (log/info "Starting up...")
  (dt/initialize (config [:datomic]))
  (log/info "Datomic initialized")
  (queue/initialize)
  (log/info "RabbitMQ initialized")
  (let [sqs-creds {:access-key    (config [:aws :creds :access-key])
                   :access-secret (config [:aws :creds :secret-key])
                   :region        (config [:aws :sqs :region])}
        messages-future (sqs/consume-messages
                         sqs-creds
                         (config [:aws :sqs :queue])
                         (config [:aws :sqs :fail-queue])
                         process-file)]
      (log/info "Started")
      messages-future))
