(ns usps-processor.importer
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [usps-processor.sqs :as sqs]
            [clojure.tools.logging :refer [info]])
  (:gen-class))

(defn process-file [message]
  (let [s3-key (:body message)]
    (info "Processing" s3-key)
    (-> s3-key
        s3/reader-from-s3
        parse/parse
        db/store-scans)
    (info "Processed" s3-key)))

(defn -main [& args]
  (info "Starting up...")
  (let [messages-future (sqs/consume-messages (sqs/client) process-file)]
    (info "Started")
    messages-future))
