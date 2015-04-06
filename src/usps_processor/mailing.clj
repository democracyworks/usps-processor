(ns usps-processor.mailing
  (:require [turbovote.datomic-toolbox :as d]
            [datomic.api :as db]))

(defn scan->mailing [scan]
  (->> scan
       :scan/mailing
       :db/id
       (db/entity (d/db))))

(defn render [mailing]
  (-> mailing
      (select-keys [:mailing/mailer-id-9 :mailing/mailer-id-6
                    :mailing/serial-number-6 :mailing/serial-number-9
                    :mailing/customer-number :mailing/routing-code])))

(defn all-scans [mailing]
  (->> mailing
       :scan/_mailing
       (sort-by :scan/time)))

(defn all-scans-since [mailing time]
  (->> mailing
       :scan/_mailing
       (filter #(not (pos? (compare time (:scan/time %)))))
       (sort-by :scan/time)))

(defn latest-scan [mailing]
  (-> mailing
      all-scans
      last))

(defn latest-scan-since [mailing time]
  (-> mailing
      (all-scans-since time)
      last))
