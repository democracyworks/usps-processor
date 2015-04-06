(ns usps-processor.mailing-test
  (:require [clojure.test :refer :all]
            [usps-processor.mailing :refer :all]))

(deftest latest-scan-test
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}]
    (testing "multiple scans"
      (let [mailing {:scan/_mailing #{old-scan new-scan}}]
        (is (= new-scan (latest-scan mailing)))))
    (testing "one scan"
      (let [mailing {:scan/_mailing #{old-scan}}]
        (is (= old-scan (latest-scan mailing)))))
    (testing "no scans"
      (let [mailing {:scan/_mailing #{}}]
        (is (nil? (latest-scan mailing)))))))

(deftest all-scans-test
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}
        mailing {:scan/_mailing [old-scan new-scan]}]
    (testing "all scans in order"
      (is (= [old-scan new-scan] (all-scans mailing)))))
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}
        mailing {:scan/_mailing [new-scan old-scan]}]
    (testing "all scans out of order"
      (is (= [old-scan new-scan] (all-scans mailing))))))

(deftest latest-scan-since-test
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}]
    (testing "multiple scans"
      (let [mailing {:scan/_mailing #{old-scan new-scan}}]
        (is (= new-scan (latest-scan-since mailing #inst "1998-01-01")))
        (is (= new-scan (latest-scan-since mailing #inst "1999-01-01")))
        (is (= new-scan (latest-scan-since mailing #inst "2014-01-01")))
        (is (empty?     (latest-scan-since mailing #inst "2015-01-01")))))
    (testing "one scan"
      (let [mailing {:scan/_mailing #{old-scan}}]
        (is (= old-scan (latest-scan-since mailing #inst "1998-01-01")))
        (is (= old-scan (latest-scan-since mailing #inst "1999-01-01")))
        (is (empty?     (latest-scan-since mailing #inst "2014-01-01")))))
    (testing "no scans"
      (let [mailing {:scan/_mailing #{}}]
        (is (empty?     (latest-scan-since mailing #inst "1999-01-01")))))))

(deftest all-scans-since-test
  (let [old-scan {:scan/time #inst "1999-01-01"}
        new-scan {:scan/time #inst "2014-01-01"}
        mailing {:scan/_mailing #{old-scan new-scan}}]
    (testing "all scans since"
      (is (= [old-scan new-scan] (all-scans-since mailing #inst "1998-01-01")))
      (is (= [old-scan new-scan] (all-scans-since mailing #inst "1999-01-01")))
      (is (= [new-scan] (all-scans-since mailing #inst "2014-01-01")))
      (is (empty? (all-scans-since mailing #inst "2015-01-01"))))))
