(ns usps-processor.importer
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [democracyworks.squishy :as sqs]
            [clojure.tools.logging :refer [info]]
            [turbovote.resource-config :refer [config]]
            [riemann.client :as riemann]
            [clojure.edn :as edn])
  (:gen-class))

(defn send-event [metric description]
  (when (config :riemann :host)
    (let [c (riemann/udp-client :host (config :riemann :host) :port (config :riemann :port))]
      (riemann/send-event c {:service "usps-processor scans" :metric metric
                             :tags ["usps-processor"]
                             :description description}
                          false))))

(defn process-file [message]
  (let [{:keys [bucket filename]} (edn/read-string (:body message))]
    (info "Processing" bucket "/" filename)
    (let [scans (parse/parse (s3/reader-from-s3 bucket filename))
          scan-count (count scans)]
      (db/store-scans scans)
      (send-event scan-count (str "Processed " scan-count " scans from " bucket))
      (info "Processed" bucket "/" filename))))

(defn -main [& args]
  (info "Starting up...")
  (let [messages-future (sqs/consume-messages (sqs/client) process-file)]
    (info "Started")
    messages-future))
