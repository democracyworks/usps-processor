(ns usps-processor.parse
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.tools.logging :as log]
            [turbovote.imbarcode :as imb]
            [clj-time.core :as time]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]
            [usps-processor.zip-lookup :as zip]
            [schema.core :as s]))

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
   'America/New_York', and returns a joda DateTime in UTC for that time."
  [time-string timezone-id]
  (try
    (let [timezone (timezone-id->timezone timezone-id)
          formatter (format/formatter usps-time-format-pattern timezone)
          time-in-zone (format/parse formatter time-string)]
      (time/to-time-zone time-in-zone time/utc))
    (catch Exception ex
      (log/warn "Exception parsing scan time" ex))))

(defn datetime->date
  "Converts a joda Datetime to java Date, or returns nil if Datetime is nil"
  [datetime]
  (when datetime
    (coerce/to-date datetime)))

(defn ->imb-data
  "Combines the structure-digits and routing code and
   splits them into the possible mailer-ids and
   serial numbers"
  [structure-digits routing-code]
  (when (and structure-digits
             (= 20 (count structure-digits)))
    (imb/split-structure-digits
     (str structure-digits routing-code))))

(defn valid-number?
  [count number]
  (let [pattern (str "\\d{" count "}?")
        regex (re-pattern pattern)]
    (not (nil? (re-matches regex number)))))

(defn valid-barcode?
  [zip]
  (valid-number? 2 zip))

(defn valid-facility-zip?
  [zip]
  (valid-number? 5 zip))

(defn valid-scan-time?
  "Checks that scan time is a java.util.Date"
  [scan-time]
  (isa? (class scan-time) java.util.Date))

(defn valid-customer-number?
  [customer-number]
  (valid-number? 15 customer-number))

(defn valid-number-3?
  [number]
  (valid-number? 3 number))

(defn valid-number-6?
  [number]
  (valid-number? 6 number))

(defn valid-number-9?
  [number]
  (valid-number? 9 number))

(defn valid-routing-number?
  [number]
  (and (not (nil? number))
       (not (nil? (re-matches #"\d*?" number)))
       (#{0 5 9 11} (count number))))

(def inbound-schema
  {:barcode (s/pred valid-barcode?)
   :service (s/pred valid-number-3?)
   :routing s/Str
   :customer-number (s/pred valid-customer-number?)})

(def outbound-schema
  {:barcode (s/pred valid-barcode?)
   :service (s/pred valid-number-3?)
   :routing s/Str
   :9-digit-mailer
   {:mailer-id (s/pred valid-number-9?)
    :serial-number (s/pred valid-number-6?)}
   :6-digit-mailer
   {:mailer-id (s/pred valid-number-6?)
    :serial-number (s/pred valid-number-9?)}})

(def scan-schema
  {:facility-zip (s/pred valid-facility-zip?)
   :operation-code (s/pred valid-number-3?)
   :scan-time (s/pred valid-scan-time?)
   :timezone-id s/Str
   :imb-data (s/conditional :customer-number
                            inbound-schema
                            :9-digit-mailer
                            outbound-schema)})

(defn row->map [row]
  (let [[facility op-code time routing-code structure-digits] row
        timezone-id (zip->timezone-id facility)
        time-utc (->time-utc time timezone-id)
        scan-time (datetime->date time-utc)
        routing-code (when routing-code (str/trim routing-code))
        imb-data (->imb-data structure-digits routing-code)
        row-data {:facility-zip facility
                  :operation-code op-code
                  :scan-time scan-time
                  :timezone-id timezone-id
                  :imb-data imb-data}]
    (try
      (s/validate scan-schema row-data)
      (catch Exception ex
        (log/error "Scan line didn't process: " row)
        (log/error "  Reason: " ex)))))

(defn parse [input]
  (->> input
       csv/read-csv
       (map row->map)
       (remove nil?)))
