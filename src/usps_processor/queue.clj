(ns usps-processor.queue
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [turbovote.resource-config :refer [config]]
            [clojure.tools.logging :refer [info]]
            [usps-processor.mailing :as mailing]
            [usps-processor.scan :as scan]))

(def channel (atom nil))

(def ^{:const true}
  events-exchange "events")

(defn initialize
  []
  (let [conn (rmq/connect (config [:rabbit-mq :connection]))
        ch   (lch/open conn)]
    (reset! channel ch)
    (le/declare ch events-exchange "topic" {:durable true :auto-delete false})))

(defn publish-scan
  [scan]
  (let [mailing (mailing/scan->mailing scan)
        rendered-scan (scan/render-scan scan)
        rendered-mailing (mailing/render mailing)
        event (merge rendered-scan rendered-mailing)
        edn (pr-str event)]
    (info "Publishing scan event to usps-scans topic: " edn)
    (lb/publish @channel events-exchange "usps-scans" edn
                {:content-type "application/edn"})))

(defn publish-scans
  [scans]
  (doseq [scan scans]
    (publish-scan scan)))
