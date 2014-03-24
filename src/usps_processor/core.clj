(ns usps-processor.core
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]
            [usps-processor.sqs :as sqs]))

(defn process-file [s3-key]
  (-> s3-key
      s3/reader-from-s3
      parse/parse
      db/store-all))

(defn -main [& args]
  (sqs/consume-messages process-file))
