(ns usps-processor.queue-test
  (:require [usps-processor.queue :refer :all]
            [clojure.test :refer :all]
            [langohr.basic :as lb]
            [datomic-toolbox.core :as dt]
            [datomic.api :as d]
            [usps-processor.db :as db]
            [clojure.edn :as edn]
            [usps-processor.test-util :refer [with-fresh-db]]))

(def test-queue (atom []))

(defn test-queue-publish
  [_ _ topic payload & _]
  (swap! test-queue #((comp vec cons) %2 %1)
         {:topic topic :payload payload}))

(defn empty-test-queue
  [f]
  (reset! test-queue [])
  (f))

(use-fixtures :each (join-fixtures [with-fresh-db empty-test-queue]))

(deftest publish-scan-test
  (with-redefs [lb/publish test-queue-publish]
    (testing "publishes rendered scan merged with rendered mailing"
      (let [scan-data {:facility-zip "80211"
                       :operation-code "895"
                       :scan-time #inst "2015-02-24T07:00:00.000-00:00"
                       :timezone-id "America/Denver"
                       :imb-data {:barcode "000" :service "040"
                                  :9-digit-mailer {:mailer-id "123456789"
                                                   :serial-number "654321"}
                                  :6-digit-mailer {:mailer-id "123456"
                                                   :serial-number "789654321"}}}
            scan-entity (db/store-scan scan-data)]

        (publish-scan scan-entity)

        (let [expected {:scan/facility-zip "80211"
                        :scan/facility-city-state {:city "Denver" :state "CO"}
                        :scan/barcode "000"
                        :scan/service "040"
                        :scan/operation-code "895"
                        :scan/time #inst "2015-02-24T07:00:00.000-00:00"
                        :scan/timezone-id "America/Denver"
                        :mailing/mailer-id-9 "123456789"
                        :mailing/serial-number-6 "654321"
                        :mailing/mailer-id-6 "123456"
                        :mailing/serial-number-9 "789654321"}
              actual (-> @test-queue first :payload edn/read-string)
              [only-actual only-expected _] (clojure.data/diff actual expected)]
          (is (empty? only-actual))
          (is (empty? only-expected)))))))
