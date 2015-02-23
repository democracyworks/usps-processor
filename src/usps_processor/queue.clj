(ns usps-processor.queue
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [turbovote.resource-config :refer [config]]
            [usps-processor.api :refer [render-scan]]
            [clojure.tools.logging :refer [info]]))

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
  [scan]
  (let [rendered-scan (render-scan scan :attach-mailing)]
    (info "Publishing scan event to usps-scans topic: " (pr-str rendered-scan))
    (lb/publish @channel events-exchange "usps-scans"
                (pr-str rendered-scan) {:content-type "application/edn"})))

(defn publish-scans
  [scans]
  (doseq [scan scans]
    (publish-scan scan)))
