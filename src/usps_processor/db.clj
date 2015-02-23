(ns usps-processor.db
  (:require [turbovote.datomic-toolbox :as d]
            [datomic.api :as db]))

(defn scan->mailing-contraints [scan-data]
  {:mailing/serial-number-6
   (get-in scan-data [:imb-data :9-digit-mailer :serial-number])
   :mailing/serial-number-9
   (get-in scan-data [:imb-data :6-digit-mailer :serial-number])
   :mailing/mailer-id-6
   (get-in scan-data [:imb-data :6-digit-mailer :mailer-id])
   :mailing/mailer-id-9
   (get-in scan-data [:imb-data :9-digit-mailer :mailer-id])
   :mailing/customer-number
   (get-in scan-data [:imb-data :customer-number])
   :mailing/routing-code
   (:routing scan-data)})

(defn scan-data->scan-tx-data
  [scan-data mailing-id]
  [{:db/id (d/tempid)
    :scan/mailing mailing-id
    :scan/facility-zip (:facility-zip scan-data)
    :scan/operation-code (:operation-code scan-data)
    :scan/time (:scan-time scan-data)
    :scan/barcode (get-in scan-data [:imb-data :barcode])
    :scan/service (get-in scan-data [:imb-data :service])}])

(def optional-mailing-keys
  [:mailing/serial-number-6
   :mailing/serial-number-9
   :mailing/mailer-id-6
   :mailing/mailer-id-9
   :mailing/customer-number])

(defn scan-data->mailing-tx-data
  [scan-data]
  (let [mailing-tx-map {:db/id (d/tempid)
                        :mailing/serial-number-6
                        (get-in scan-data [:imb-data :9-digit-mailer :serial-number])

                        :mailing/mailer-id-9
                        (get-in scan-data [:imb-data :9-digit-mailer :mailer-id])

                        :mailing/serial-number-9
                        (get-in scan-data [:imb-data :6-digit-mailer :serial-number])

                        :mailing/mailer-id-6
                        (get-in scan-data [:imb-data :6-digit-mailer :mailer-id])

                        :mailing/customer-number
                        (get-in scan-data [:imb-data :customer-number])}]
    [(d/dissoc-optional-nil-values mailing-tx-map optional-mailing-keys)]))

(defn store-scan
  [scan-data]
  (let [existing-mailing-id (->> scan-data
                                 scan->mailing-contraints
                                 (d/match-entities (d/db))
                                 first
                                 :db/id)
        mailing-tx-data (if existing-mailing-id [] (scan-data->mailing-tx-data scan-data))
        mailing-id (or existing-mailing-id (some :db/id mailing-tx-data))
        scan-tx-data (scan-data->scan-tx-data scan-data mailing-id)
        scan-id (some :db/id scan-tx-data)
        tx @(d/transact (concat mailing-tx-data scan-tx-data))]
    (db/entity (:db-after tx)
               (db/resolve-tempid (:db-after tx) (:tempids tx) scan-id))))

(defn store-scans [scans]
  (doall (map store-scan scans)))
