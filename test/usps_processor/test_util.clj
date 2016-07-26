(ns usps-processor.test-util
  (:require  [clojure.test :refer :all]
             [datomic.api :as d]
             [datomic-toolbox.core :as db]
             [turbovote.resource-config :refer [config]]))

(defn with-fresh-db [test]
  (let [uri (str "datomic:mem://usps-processor-"
                 (java.util.UUID/randomUUID))]
    (try
     (db/initialize
      (assoc (config [:datomic]) :uri uri))
     (test)
     (finally (d/delete-database uri)))))
