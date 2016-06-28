(ns usps-processor.parse
  (:require [clojure.string :as s]
            [clojure.data.csv :as csv]
            [clojure.tools.logging :as log]
            [turbovote.imbarcode :as imb]
            [clj-time.core :as time]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]
            [usps-processor.zip-lookup :as zip]))

(def default-timezone-id "America/Los_Angeles")
(def usps-time-format-pattern "M/d/yyyy H:mm:ss")

(defn zip->timezone-id
  "Looks up the id of the timezone for the provided zip."
  [zip]
  (or (some-> zip
              zip/zipcode->city-state-timezone
              :timezone-id)
      default-timezone-id))

(defn timezone-id->timezone
  "Gets the TimeZone for the given timezone-id (e.q. 'America/Seattle'),
   and if none matches, uses the default-timezone-id defined above."
  [timezone-id]
  (try
    (time/time-zone-for-id timezone-id)
    (catch Exception ex
      (log/warn "No TimeZone found matching id: " timezone-id)
      (time/time-zone-for-id default-timezone-id))))

(defn ->time-utc
  "Takes the time in the USPS string format, and a timezone id like
   'America/New_York', and returns a DateTime in UTC for that time."
  [time-string timezone-id]
  (let [timezone (timezone-id->timezone timezone-id)
        formatter (format/formatter usps-time-format-pattern timezone)
        time-in-zone (format/parse formatter time-string)]
    (time/to-time-zone time-in-zone time/utc)))

(defn row->map [row]
  (let [[facility op-code time routing-code structure-digits] row
        timezone-id (zip->timezone-id facility)
        time-utc (->time-utc time timezone-id)
        routing-code (s/trim routing-code)]
    {:facility-zip facility
     :operation-code op-code
     :scan-time (coerce/to-date time-utc)
     :timezone-id timezone-id
     :imb-data (imb/split-structure-digits
                (str structure-digits routing-code))}))

(defn parse [input]
  (->> input
       csv/read-csv
       (map row->map)))
