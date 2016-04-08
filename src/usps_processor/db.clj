(ns usps-processor.db
  (:require [datomic-toolbox.core :as dt]
            [datomic-toolbox.transaction :as dtx]
            [datomic-toolbox.query :as dtq]
            [datomic.api :as d]))

(defn scan->mailing-constraints [scan-data]
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
  [{:db/id (dt/tempid)
    :scan/mailing mailing-id
    :scan/facility-zip (:facility-zip scan-data)
    :scan/operation-code (:operation-code scan-data)
    :scan/time (:scan-time scan-data)
    :scan/timezone-id (:timezone-id scan-data)
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
  (let [mailing-tx-map {:db/id (dt/tempid)
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
    [(dtx/dissoc-optional-nil-values mailing-tx-map optional-mailing-keys)]))

(defn store-scan
  [scan-data]
  (let [existing-mailing-id (->> scan-data
                                 scan->mailing-constraints
                                 (dtq/match-entities (dt/db))
                                 first
                                 :db/id)
        mailing-tx-data (if existing-mailing-id [] (scan-data->mailing-tx-data scan-data))
        mailing-id (or existing-mailing-id (some :db/id mailing-tx-data))
        scan-tx-data (scan-data->scan-tx-data scan-data mailing-id)
        scan-id (some :db/id scan-tx-data)
        tx @(dt/transact (concat mailing-tx-data scan-tx-data))]
    (d/entity (:db-after tx)
              (d/resolve-tempid (:db-after tx) (:tempids tx) scan-id))))

(defn store-scans [scans]
  (doall (map store-scan scans)))
