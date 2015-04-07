(ns usps-processor.api
  (:require [usps-processor.mailing :as mailing]
            [usps-processor.zip-lookup :refer [zipcode->city-state]]
            [turbovote.datomic-toolbox :as d]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [org.httpkit.server :refer [run-server]]
            [turbovote.resource-config :refer [config]]
            [clojure.set :as set]
            [clojure.tools.logging :refer [info]]
            [democracyworks.squishy.data-readers]
            [clojure.instant :as instant])
  (:gen-class))

(def param->query-key
  {:serial-6 :mailing/serial-number-6
   :mailer-9 :mailing/mailer-id-9
   :serial-9 :mailing/serial-number-9
   :mailer-6 :mailing/mailer-id-6
   :customer-number :mailing/customer-number
   :routing-code :mailing/routing-code})

(defn params->mailing-constraints
  [params]
  (-> params
      (select-keys (keys param->query-key))
      (set/rename-keys param->query-key)))

(defn edn-response [body]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (pr-str body)})

(defn attach-facility-city-state [scan]
  (assoc scan
    :scan/facility-city-state
    (some-> scan
            :scan/facility-zip
            (Integer/parseInt)
            zipcode->city-state)))

(defn render-scan
  [scan]
  (-> scan
      (select-keys [:scan/time :scan/barcode :scan/facility-zip
                    :scan/operation-code :scan/service])
      attach-facility-city-state))

(defn lookup-mailings [req]
  (let [db (d/db)
        mailing-constraints (-> req :params params->mailing-constraints)
        mailings (d/match-entities db mailing-constraints)]
    mailings))

(def standard-not-found
  (-> "Not found"
      edn-response
      (assoc :status 404)))

(def standard-multiple-matches
  (-> "Multiple matches found"
      edn-response
      (assoc :status 422)))

(defn get-scanned-since [req]
  (when-let [string (get-in req [:params :scanned-since])]
    (instant/read-instant-date string)))

(defn latest-scan [req]
  (let [matches (lookup-mailings req)]
    (cond
       (empty? matches)
       standard-not-found

       (> (count matches) 1)
       standard-multiple-matches

       (not (get-scanned-since req))
       (edn-response (render-scan (mailing/latest-scan (first matches))))

       :else
       (if-let [the-scan (mailing/latest-scan-since (first matches) (get-scanned-since req))]
         (edn-response (render-scan the-scan))
         standard-not-found))))

(defn all-scans [req]
  (let [matches (lookup-mailings req)]
    (cond
       (empty? matches)
       standard-not-found

       (> (count matches) 1)
       standard-multiple-matches

       (not (get-scanned-since req))
       (edn-response (map render-scan (mailing/all-scans (first matches))))

       :else
       (let [all-the-scans (mailing/all-scans-since (first matches) (get-scanned-since req))]
         (edn-response (map render-scan all-the-scans))))))

(defroutes app
  (GET "/ping" [] "pong!")
  (GET "/latest-scan" [] latest-scan)
  (GET "/all-scans" [] all-scans))

(defn -main [& args]
  (let [port (config :api :port)]
    (info "API Server starting up on port" port)
    (run-server (site app) {:port port})))
