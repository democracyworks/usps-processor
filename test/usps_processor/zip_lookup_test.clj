(ns usps-processor.zip-lookup-test
  (:require [clojure.test :refer :all]
            [usps-processor.zip-lookup :refer :all]))

(deftest proper-case-test
  (testing "properly cases a place name"
    (is (= "Brooklyn" (proper-case "BROOKLYN")))
    (is (= "New York" (proper-case "NEW YORK")))
    (is (= "Grand View-on-Hudson" (proper-case "GRAND VIEW-ON-HUDSON")))))
