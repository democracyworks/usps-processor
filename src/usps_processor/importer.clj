(ns usps-processor.importer
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [usps-processor.mailing :as mailing]
            [usps-processor.scan :as scan]
            [usps-processor.channels :as channels]
            [squishy.core :as sqs]
            [clojure.tools.logging :as log]
            [turbovote.resource-config :refer [config]]
            [datomic-toolbox.core :as dt]
            [clojure.edn :as edn]
            [clojure.core.async :as async])
  (:gen-class))

(defn publish-scan
  [scan]
  (let [mailing (mailing/scan->mailing scan)
        rendered-scan (scan/render-scan scan)
        rendered-mailing (mailing/render mailing)
        event (merge rendered-scan rendered-mailing)]
    (log/debug "Publishing scan event to usps-scans topic:" (pr-str event))
    (async/>!! channels/usps-scans-out event)))

(defn publish-scans
  [scans]
  (doseq [scan scans]
    (publish-scan scan)))

(defn process-file [message]
  (let [{:keys [bucket filename]} (edn/read-string (:body message))]
    (log/debug "Processing" bucket "/" filename)
    (let [scans (parse/parse (s3/reader-from-s3 bucket filename))
          scan-count (count scans)
          stored-scans (db/store-scans scans)]
      (publish-scans stored-scans)
      (log/debug "Processed" scan-count "scans from" bucket "/" filename))))

(defn start-message-consumer []
  (let [sqs-creds {:access-key    (config [:aws :creds :access-key])
                   :access-secret (config [:aws :creds :secret-key])
                   :region        (config [:aws :sqs :region])}
        cid (sqs/consume-messages
                         sqs-creds
                         (config [:aws :sqs :queue])
                         (config [:aws :sqs :fail-queue])
                         process-file)]
    (log/info "Consuming SQS messages")
    cid))
