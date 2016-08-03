(ns usps-processor.channels
  (:require [clojure.core.async :as async]))

(defonce usps-scans-out (async/chan 1000))

(defn close-all! []
  (doseq [c [usps-scans-out]]
    (async/close! c)))
