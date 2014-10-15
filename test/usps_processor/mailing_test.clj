(ns usps-processor.mailing-test
  (:require [clojure.test :refer :all]
            [usps-processor.mailing :refer :all]))

(deftest latest-scan-test
  (let [old-scan {:scan/time #inst "1999-01-01" :scan/facility-zip "11215"}
        new-scan {:scan/time #inst "2014-01-01" :scan/facility-zip "06850"}]
    (testing "multiple scans"
      (let [mailing {:scan/_mailing #{old-scan new-scan}}]
        (is (= (assoc new-scan
                 :scan/facility-city-state {:city "NORWALK" :state "CT"})
               (latest-scan mailing)))))
    (testing "one scan"
      (let [mailing {:scan/_mailing #{old-scan}}]
        (is (= (assoc old-scan
                 :scan/facility-city-state {:city "BROOKLYN" :state "NY"})
               (latest-scan mailing)))))
    (testing "no scans"
      (let [mailing {:scan/_mailing #{}}]
        (is (nil? (latest-scan mailing)))))))

(deftest all-scans-test
  (let [old-scan {:scan/time #inst "1999-01-01" :scan/facility-zip "11215"}
        new-scan {:scan/time #inst "2014-01-01" :scan/facility-zip "06850"}
        mailing {:scan/_mailing #{old-scan new-scan}}]
    (testing "all scans"
      (is (= [(assoc old-scan
                :scan/facility-city-state {:city "BROOKLYN" :state "NY"})
              (assoc new-scan
                :scan/facility-city-state {:city "NORWALK" :state "CT"})]
             (all-scans mailing))))))
