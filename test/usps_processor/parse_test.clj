(ns usps-processor.parse-test
  (:require [clojure.test :refer :all]
            [usps-processor.parse :refer :all]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clj-time.format :as format]))

(deftest zip->timezone-id-test
  (testing "with a zipcode in our data set"
    (is (= "America/Denver" (zip->timezone-id "80202"))))
  (testing "with a zipcode not in our data set"
    (is (= "America/Los_Angeles" (zip->timezone-id "00001")))))

(deftest timezone-id->timezone-test
  (testing "with a valid timezone id"
    (is (= "America/Denver"
           (-> (timezone-id->timezone "America/Denver") .getID))))
  (testing "with an invalid timezone id, we default to Los Angeles (for now)"
    (is (= "America/Los_Angeles"
           (-> (timezone-id->timezone "America/Walla_Walla") .getID)))))

(deftest ->time-utc-test
  (testing "with a Denver Time"
    ;; Note how the Denver time is 1:58 PM, which is 8:58 PM UTC
    (is (= (time/date-time 2016 4 7 20 58 0 0))
        (->time-utc "04/07/2016 13:58:00" "America/Denver")))
  (testing "defaults to LA time if the time zone id isn't a known one"
    (is (= (time/date-time 2016 4 7 21 58 0 0))
        (->time-utc "04/07/2016 13:58:00" "America/Walla_Walla"))))

(deftest valid-number?-test
  (testing "passes when all digits and match count"
    (is (valid-number? 5 "12345")))
  (testing "fails when not all digits"
    (is (not (valid-number? 5 "a2345"))))
  (testing "fails when count isn't met"
    (is (not (valid-number? 5 "1234")))
    (is (not (valid-number? 5 "123456")))))

(deftest valid-scan-time?-test
  (testing "passes with a java.util.Date"
    (is (valid-scan-time? (java.util.Date.))))
  (testing "fails with nil"
    (is (not (valid-scan-time? nil)))))

(deftest valid-routing-number?-test
  (testing "passes with blank"
    (is (valid-routing-number? "")))
  (testing "passes with 5 digits"
    (is (valid-routing-number? "12345")))
  (testing "passes with 9 digits"
    (is (valid-routing-number? "123456789")))
  (testing "passes with 11 digits"
    (is (valid-routing-number? "12345678901")))
  (testing "fail cases"
    (is (not (valid-routing-number? "12")))
    (is (not (valid-routing-number? "a1234")))
    (is (not (valid-routing-number? nil)))))

(deftest row->map-test
  (testing "valid outbound row, blank routing code"
    (let [data (row->map ["80204" "141" "07/20/2016 12:38:20"
                          " " "00040123456789123456"])]
      (is (= "123456789" (get-in data [:imb-data :9-digit-mailer :mailer-id])))
      (is (= "123456" (get-in data [:imb-data :6-digit-mailer :mailer-id])))
      (is (nil? (get-in data [:imb-data :customer-number])))
      (is (= "00" (get-in data [:imb-data :barcode])))
      (is (= "" (get-in data [:imb-data :routing])))
      (is (= "America/Denver" (:timezone-id data)))))
  (testing "valid outbound row, 5 digit routing code"
    (let [data (row->map ["80204" "141" "07/20/2016 12:38:20"
                          "80204      " "00040123456789123456"])]
      (is (= "80204" (get-in data [:imb-data :routing])))))
  (testing "valid inbound row, 11 digit routing code"
    (let [data (row->map ["80204" "141" "07/20/2016 12:38:20"
                          "80204123456" "00050123456789123456"])]
      (is (= "123456789123456" (get-in data [:imb-data :customer-number])))
      (is (nil? (get-in data [:imb-data :9-digit-mailer :mailer-id])))
      (is (nil? (get-in data [:imb-data :6-digit-mailer :mailer-id])))
      (is (= "00" (get-in data [:imb-data :barcode])))
      (is (= "80204123456" (get-in data [:imb-data :routing])))
      (is (= "America/Denver" (:timezone-id data)))))
  (testing "mangled time"
    (let [data (row->map ["80204" "141" "037/20/2016 12:38:20"
                          "80204123456" "00050123456789123456"])]
      (is (nil? data))))
  (testing "mangled structured digits"
    (let [data (row->map ["80204" "141" "037/20/2016 12:38:20"
                          "80204123456" "000501"])]
      (is (nil? data)))
    (let [data (row->map ["80204" "141" "07/20/2016 12:38:20"
                          "80204123456" "000501234567891234567"])]
      (is (nil? data))))
  (testing "mangled row"
    (let [data (row->map ["8020"])]
      (is (nil? data))))
  (testing "empty row"
    (let [data (row->map [])]
      (is (nil? data)))))
