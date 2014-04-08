(ns immutant.init
  (:require [immutant.daemons :as daemon]
            [immutant.web :as web]
            [compojure.handler :refer [site]]
            [usps-processor.api :as api]
            [usps-processor.importer :as importer]))

(def messages-future (atom nil))

(defn start []
  (reset! messages-future (importer/-main)))

(defn stop []
  (swap! messages-future (fn [f] (when f (future-cancel f)))))

(daemon/daemonize "usps-processor.importer" start stop :singleton false)

(web/start (site api/app))
