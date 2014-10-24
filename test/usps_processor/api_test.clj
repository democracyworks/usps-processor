(ns usps-processor.api-test
  (:require [clojure.test :refer :all]
            [usps-processor.api :refer :all]))

(deftest on-single-match-test
  (testing "200 response"
    (let [resp (on-single-match [1] inc)]
      (is (= (:status resp) 200))
      (is (= (:body resp) "2"))))
  (testing "404 response"
    (let [resp (on-single-match [] identity)]
      (is (= (:status resp) 404))
      (is (= (:body resp) "\"Not found\""))))
  (testing "422 response"
    (let [resp (on-single-match [:one :two] identity)]
      (is (= (:status resp) 422))
      (is (= (:body resp) "\"Multiple matches found\"")))))

(deftest render-scan-test
  (testing "attaches the scan location"
    (let [scan {:scan/facility-zip "11215"}]
      (is (= (render-scan scan)
             {:scan/facility-zip "11215"
              :scan/facility-city-state {:city "Brooklyn" :state "NY"}}))))
  (testing "attaches the correct location for a 0-leading zipcode"
    (let [scan {:scan/facility-zip "06850"}]
      (is (= (render-scan scan)
             {:scan/facility-zip "06850"
              :scan/facility-city-state {:city "Norwalk" :state "CT"}})))))
