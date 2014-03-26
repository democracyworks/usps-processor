(ns usps-processor.mailing)

(defn latest-scan [mailing]
  (->> mailing
       :scan/_mailing
       (sort-by :scan/time)
       last))

(defn render [mailing]
  (select-keys mailing [:scan/time :scan/barcode :scan/facility-zip
                        :scan/operation-code :scan/service]))
