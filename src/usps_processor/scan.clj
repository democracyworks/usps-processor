(ns usps-processor.scan
  (:require [phantom-zone.core :as zip]))

(defn attach-facility-city-state [scan]
  (assoc scan
    :scan/facility-city-state
    (some-> scan
            :scan/facility-zip
            zip/zipcode->city-state-timezone
            (select-keys [:city :state]))))

(defn render-scan
  [scan]
  (-> scan
      (select-keys [:scan/time :scan/timezone-id :scan/barcode
                    :scan/facility-zip :scan/operation-code :scan/service])
      attach-facility-city-state))
