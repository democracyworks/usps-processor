(ns usps-processor.mailing)

(defn latest-scan [mailing]
  (->> mailing
       :scan/_mailing
       (sort-by :scan/time)
       last))
