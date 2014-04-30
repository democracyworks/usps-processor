(ns usps-processor.importer
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [usps-processor.sqs :as sqs]
            [clojure.tools.logging :refer [info]]
            [turbovote.resource-config :refer [config]]
            [riemann.client :as riemann])
  (:gen-class))

(defn send-event [metric]
  (when (config :riemann :host)
    (let [c (riemann/udp-client :host (config :riemann :host) :port (config :riemann :port))]
      (riemann/send-event c {:service "usps-processor scans" :metric metric
                             :tags ["usps-processor"]
                             :description (str "Processed " metric " scans from "
                                               (config :aws :s3 :bucket))}
                          false))))

(defn process-file [message]
  (let [s3-key (:body message)]
    (info "Processing" s3-key)
    (let [scans (-> s3-key
                    s3/reader-from-s3
                    parse/parse)]
      (db/store-scans scans)
      (send-event (count scans))
      (info "Processed" s3-key))))

(defn -main [& args]
  (info "Starting up...")
  (let [messages-future (sqs/consume-messages (sqs/client) process-file)]
    (info "Started")
    messages-future))
