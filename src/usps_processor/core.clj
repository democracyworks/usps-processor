(ns usps-processor.core
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [usps-processor.sqs :as sqs]
            [clojure.tools.logging :refer [info]]))

(defn process-file [s3-key]
  (info "Processing" s3-key)
  (-> s3-key
      s3/reader-from-s3
      parse/parse
      db/store-all)
  (info "Processed" s3-key))

(defn -main [& args]
  (info "Starting up...")
  (sqs/consume-messages process-file)
  (info "Started"))
