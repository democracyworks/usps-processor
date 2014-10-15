(ns usps-processor.mailing)


(defn all-scans [mailing]
  (->> mailing
       :scan/_mailing
       (sort-by :scan/time)))

(defn latest-scan [mailing]
  (-> mailing
      all-scans
      last))
