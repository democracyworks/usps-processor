(ns usps-processor.scan
  (:require [usps-processor.zip-lookup :as zip]))

(defn attach-facility-city-state [scan]
  (assoc scan
    :scan/facility-city-state
    (some-> scan
            :scan/facility-zip
            (Integer/parseInt)
            zip/zipcode->city-state-timezone
            (select-keys [:city :state]))))

(defn render-scan
  [scan]
  (-> scan
      (select-keys [:scan/time :scan/timezone-id :scan/barcode
                    :scan/facility-zip :scan/operation-code :scan/service])
      attach-facility-city-state))
