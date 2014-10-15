(ns usps-processor.mailing-test
  (:require [clojure.test :refer :all]
            [usps-processor.mailing :refer :all]))

(deftest latest-scan-test
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}]
    (testing "multiple scans"
      (let [mailing {:scan/_mailing #{old-scan new-scan}}]
        (is (=  new-scan (latest-scan mailing)))))
    (testing "one scan"
      (let [mailing {:scan/_mailing #{old-scan}}]
        (is (= old-scan (latest-scan mailing)))))
    (testing "no scans"
      (let [mailing {:scan/_mailing #{}}]
        (is (nil? (latest-scan mailing)))))))

(deftest all-scans-test
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}
        mailing {:scan/_mailing #{old-scan new-scan}}]
    (testing "all scans"
      (is (= [old-scan new-scan] (all-scans mailing))))))
