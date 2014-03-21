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

(defn find-mailing
  [scan-data]
  (first (d/match-entities (d/db) {:mailing/serial-number-6
                                  (get-in scan-data [:imb-data
                                                     :9-digit-mailer
                                                     :serial-number])
                                 :mailing/serial-number-9
                                  (get-in scan-data [:imb-data
                                                     :6-digit-mailer
                                                     :serial-number])
                                 :mailing/mailer-id-6
                                  (get-in scan-data [:imb-data
                                                     :6-digit-mailer
                                                     :mailer])
                                 :mailing/mailer-id-9
                                  (get-in scan-data [:imb-data
                                                     :9-digit-mailer
                                                     :mailer])
                                 :mailing/customer-number
                                   (get-in scan-data [:imb-data
                                                      :customer-number])
                                 :mailing/routing-code
                                  (:routing scan-data)})))

(defn scan-data->scan-tx-data
  [scan-data mailing-id]
  [{:scan/mailing mailing-id
    :scan/facility-zip (:facility-zip scan-data)
    :scan/operation-code (:operation-code scan-data)
    :scan/time (:scan-time scan-data)
    :scan/barcode (get-in scan-data [:imb-data :barcode])
    :scan/service (get-in scan-data [:imb-data :service])}])

(defn scan-data->mailing-tx-data
  [scan-data]
  [{:db/id (d/tempid)
    :mailing/serial-number-6
    (get-in scan-data [:imb-data :6-digit-mailer :mailer])

    :mailing/mailer-id-9
    (get-in scan-data [:imb-data :6-digit-mailer :serial-number])

    :mailing/serial-number-9
    (get-in scan-data [:imb-data :9-digit-mailer :mailer])

    :mailing/mailer-id-6
    (get-in scan-data [:imb-data :9-digit-mailer :serial-number])

    :mailing/customer-number
    (get-in scan-data [:imb-data :customer-number])}])

(defn store-usps-data
  [scan-data]
  (let [mailing (or (find-mailing scan-data)
                    (scan-data->mailing-tx-data scan-data))]
    (d/transact (scan-data->scan-tx-data (:db/id mailing)))))
