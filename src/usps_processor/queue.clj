(ns usps-processor.queue
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [turbovote.resource-config :refer [config]]))

(def channel (atom nil))

(def ^{:const true}
  events-exchange "events")

(defn initialize
  []
  (let [conn (rmq/connect (config :rabbit-mq :connection))
        ch   (lch/open conn)]
    (reset! channel ch)
    (le/declare ch events-exchange "topic" {:durable true :auto-delete false})))

(defn publish-scan
  [scan-data]
  (lb/publish @channel events-exchange "usps-scans" (pr-str scan-data)))

(defn publish-scans
  [scans]
  (doseq [scan-data scans]
    (publish-scan scan-data)))
