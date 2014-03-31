(ns usps-processor.api
  (:require [usps-processor.data-readers]
            [usps-processor.mailing :as mailing]
            [turbovote.datomic-toolbox :as d]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [org.httpkit.server :refer [run-server]]
            [turbovote.resource-config :refer [config]]
            [clojure.set :as set]
            [clojure.tools.logging :refer [info]])
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

(defn latest-scan [req]
  (let [db (d/db)
        mailing-constraints (-> req :params params->mailing-constraints)
        mailings (d/match-entities db mailing-constraints)]
    (case (count mailings)
      1 (-> mailings
            first
            mailing/latest-scan
            mailing/render
            edn-response)
      0 (-> "Not found"
          edn-response
          (assoc :status 404))
      (-> "Multiple matches found"
          edn-response
          (assoc :status 422)))))

(defroutes app
  (GET "/ping" [] "pong!")
  (GET "/latest-scan" [] latest-scan))

(defn -main [& args]
  (let [port (config :api :port)]
    (info "API Server starting up on port" port)
    (run-server (site app) {:port port})))
