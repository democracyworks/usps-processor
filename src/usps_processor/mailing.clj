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
                    :mailing/serial-number-6 :mailing/serial-number-9])))

(defn all-scans [mailing]
  (->> mailing
       :scan/_mailing
       (sort-by :scan/time)))

(defn latest-scan [mailing]
  (-> mailing
      all-scans
      last))
