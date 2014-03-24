(ns usps-processor.parse
  (:require [clojure.string :as s]
            [clojure.data.csv :as csv]
            [turbovote.imbarcode :as imb]))

(defn row->map [row]
  (let [[facility op-code time routing-code structure-digits] row
        time (java.util.Date. time)
        routing-code (s/trim routing-code)]
    {:facility-zip facility
     :operation-code op-code
     :scan-time time
     :imb-data (imb/split-structure-digits
                (str structure-digits routing-code))}))

(defn parse [input]
  (->> input
       csv/read-csv
       (map row->map)))
