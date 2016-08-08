(ns usps-processor.importer-test
  (:require [usps-processor.importer :refer :all]
            [usps-processor.channels :as channels]
            [clojure.test :refer :all]
            [usps-processor.db :as db]
            [clojure.edn :as edn]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [usps-processor.test-util :refer [with-fresh-db]]))

(defn empty-channel
  [chan]
  (loop []
    (when-not (nil? (async/poll! chan))
      (recur))))

(use-fixtures :each with-fresh-db)

(deftest publish-scan-test
  (testing "publishes rendered scan merged with rendered mailing"
    (empty-channel channels/usps-scans-out)
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
                      :mailing/serial-number-9 "789654321"}]
        (async/alt!!
          channels/usps-scans-out
          ([v] (is (=  (-> v edn/read-string) expected)))

          (async/timeout 500)
          (throw (RuntimeException. "test failed")))))))
