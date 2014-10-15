(ns usps-processor.mailing
  (:require [clojure.edn :as edn]))

(def zipcode->city-state
  (-> "zipcode-city-state.edn"
      clojure.java.io/resource
      slurp
      edn/read-string))

(defn attach-facility-city-state [scan]
  (assoc scan
    :scan/facility-city-state
    (some-> scan
            :scan/facility-zip
            (Integer/parseInt)
            zipcode->city-state)))

(defn all-scans [mailing]
  (->> mailing
       :scan/_mailing
       (map attach-facility-city-state)
       (sort-by :scan/time)))

(defn latest-scan [mailing]
  (-> mailing
      all-scans
      last))
