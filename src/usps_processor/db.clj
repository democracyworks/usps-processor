(ns usps-processor.db
  (:require [turbovote.datomic-toolbox :as d]))

(defn find-mailing
  [scan-data]
  (first
   (d/match-entities (d/db)
                     {:mailing/serial-number-6
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
                        (get-in scan-data [:imb-data :6-digit-mailer :mailer])

                        :mailing/mailer-id-9
                        (get-in scan-data [:imb-data :6-digit-mailer :serial-number])

                        :mailing/serial-number-9
                        (get-in scan-data [:imb-data :9-digit-mailer :mailer])

                        :mailing/mailer-id-6
                        (get-in scan-data [:imb-data :9-digit-mailer :serial-number])

                        :mailing/customer-number
                        (get-in scan-data [:imb-data :customer-number])}]
    [(d/dissoc-optional-nil-values mailing-tx-map optional-mailing-keys)]))

(defn store-usps-data
  [scan-data]
  (let [existing-mailing-id (-> scan-data find-mailing :db/id)
        mailing-tx (if existing-mailing-id [] (scan-data->mailing-tx-data scan-data))
        mailing-id (or existing-mailing-id (some :db/id mailing-tx))]
    (d/transact (concat mailing-tx
                        (scan-data->scan-tx-data scan-data mailing-id)))))

(defn store-all [scans]
  (doseq [scan-data scans]
    (store-usps-data scan-data)))
