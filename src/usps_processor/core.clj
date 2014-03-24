(ns usps-processor.core
  (:require [usps-processor.s3 :as s3]
            [usps-processor.parse :as parse]
            [usps-processor.db :as db]))

(defn process-file [s3-key]
  (-> s3-key
      s3/reader-from-s3
      parse/parse
      db/store-all))
