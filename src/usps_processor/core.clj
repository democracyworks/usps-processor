(ns usps-processor.core
  (:require [clojure.string :as s]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [turbovote.imbarcode :as imb]
            [aws.sdk.s3 :as s3]
            [turbovote.resource-config :refer [config]]
            [turbovote.datomic-toolbox :as d]))

(defn row->map [row]
  (let [[facility op-code time routing-code structure-digits] row
        time (java.util.Date. time)
        routing-code (s/trim routing-code)]
    {:facility-zip facility
     :operation-code op-code
     :scan-time time
     :imb-data (imb/split-structure-digits
                (str structure-digits routing-code))}))

(defn parsed-data [f]
  (->> f
       csv/read-csv
       (map row->map)))

(defn usps-data-from-s3 [key]
  (-> (s3/get-object (config :s3 :creds)
                     (config :s3 :bucket)
                     key)
      :content
      io/reader
      parsed-data))
