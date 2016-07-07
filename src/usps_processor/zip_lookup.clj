(ns usps-processor.zip-lookup
  (:require [clojure.edn :as edn]))

(defn single-proper-case [s]
  (if (#{"of" "in" "on"} (.toLowerCase s))
    (.toLowerCase s)
    (str (.toUpperCase (subs s 0 1))
         (.toLowerCase (subs s 1)))))

(defn proper-case [s]
  (apply str
         (map single-proper-case
              (map (partial apply str)
                   (partition-by #{\space \-} s)))))

(def zipcode->city-state-timezone
  (let [data (-> "zip_city_state_timezone.edn"
                 clojure.java.io/resource
                 slurp
                 edn/read-string)]
    (into {}
          (for [[zip {:keys [city state timezone-id]}] data]
            [zip {:city (proper-case city) :state state
                  :timezone-id timezone-id}]))))
