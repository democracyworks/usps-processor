(ns dev.db
  (:require [datomic-toolbox.core :as db]
            [democracyworks.squishy.data-readers]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [turbovote.resource-config :refer [config]]))

(defn read-resource [resource-path]
  (->> resource-path
       clojure.java.io/resource
       slurp
       (edn/read-string {:readers *data-readers*})))

(def seed-tx-data
  (read-resource "seed.edn"))

(defn seed-db []
  (db/transact seed-tx-data))

(defn reset-db! []
  ;; TODO: This isn't really correct. In Datomic you shouldn't delete and
  ;; re-create the same database. It's against the grain of Datomic's immutable
  ;; nature. And it does actually cause unpredictable behavior.
  (d/delete-database (config [:datomic :uri]))
  (db/initialize (config [:datomic]))
  (seed-db))

(defn -main []
  (println "Resetting the database")
  (reset-db!)
  (println "We did it!")
  (System/exit 0))
