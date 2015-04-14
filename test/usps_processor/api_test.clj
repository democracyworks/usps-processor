(ns usps-processor.api-test
  (:require [clojure.test :refer :all]
            [usps-processor.api :refer :all]
            [turbovote.datomic-toolbox :as d]
            [datomic.api :as db]
            [turbovote.resource-config :refer [config]]))

(use-fixtures :each (fn [test]
                      (db/delete-database (config :datomic :uri))
                      (d/initialize)
                      ;; ensure db is empty
                      (assert (= 404 (:status (latest-scan {:params {:customer-number "123"}}))))
                      (let [mailing-id (db/tempid :db.part/user)
                            scan1 (db/tempid :db.part/user)
                            scan2 (db/tempid :db.part/user)
                            scan3 (db/tempid :db.part/user)]
                        @(db/transact (d/connection) [{:mailing/customer-number "123" :db/id mailing-id}
                                                      {:scan/time #inst "1999-01-01" :db/id scan1
                                                       :scan/mailing mailing-id}
                                                      {:scan/time #inst "2000-01-01" :db/id scan2
                                                       :scan/mailing mailing-id}
                                                      {:scan/time #inst "2001-01-01" :db/id scan3
                                                       :scan/mailing mailing-id}]))
                      (test)
                      (db/delete-database (config :datomic :uri))))

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

(deftest latest-scan-test
  (testing "non-existing customer-number returns 404"
    (is (= 404 (:status (latest-scan {:params {:customer-number "234324"}})))))
  (testing "existing customer-number returns 200"
    (is (= 200 (:status (latest-scan {:params {:customer-number "123"}})))))
  (testing "returns latest scan when no scanned-since date passed in"
    (is (= #inst "2001-01-01" (:scan/time
                               (clojure.edn/read-string
                                (:body (latest-scan {:params {:customer-number "123"}})))))))
  (testing "returns no scan older than scanned-since date"
    (is (= 404 (:status (latest-scan {:params {:customer-number "123" :scanned-since "2015-01-01"}})))))
  (testing "returns latest scan and not the scan older than the scanned-since date"
    (is (= #inst "2001-01-01" (:scan/time
                               (clojure.edn/read-string
                                (:body (latest-scan {:params {:customer-number "123" :scanned-since "2000-07-01"}}))))))))

(deftest all-scans-test
  (testing "non-existing customer-number returns 404"
    (is (= 404 (:status (all-scans {:params {:customer-number "234324"}})))))
  (testing "existing customer-number returns 200"
    (is (= 200 (:status (all-scans {:params {:customer-number "123"}})))))
  (testing "returns all scans when no scanned-since date passed in"
    (is (= [#inst "1999-01-01" #inst "2000-01-01" #inst "2001-01-01"]
           (map :scan/time
                (clojure.edn/read-string
                 (:body (all-scans {:params {:customer-number "123"}})))))))
  (testing "returns all scans since scanned-since date"
    (is (= [#inst "2000-01-01" #inst "2001-01-01"]
           (map :scan/time
                (clojure.edn/read-string
                 (:body (all-scans {:params {:customer-number "123"
                                             :scanned-since "1999-12-01"}})))))))
  (testing "returns no scans older than scanned-since date"
    (is (= 404
           (:status (all-scans {:params {:customer-number "123"
                                         :scanned-since "2015-12-01"}}))))))
