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
