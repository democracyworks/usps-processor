(ns usps-processor.core
  (:require [usps-processor.api :as api]
            [usps-processor.importer :as importer]
            [immutant.web :as web]
            [immutant.util]
            [clojure.tools.logging :refer [info]])
  (:gen-class))

(def messages-future (atom nil))
(def api-server (atom nil))

(defn start-importer []
  (swap! messages-future (fn [mf]
                           (when mf (future-cancel mf))
                           (importer/-main) ;; returns a new future
                           )))

(defn stop-importer []
  (swap! messages-future (fn [mf]
                           (when mf
                             (future-cancel mf))
                           nil)))

(defn -main [& args]
  (info "Starting importer thread")
  (.start (Thread. start-importer))
  (immutant.util/at-exit stop-importer)
  (info "Starting api server")
  (api/-main))
