# usps-processor

## Setup

The valid values for `:aws :sqs :region` are the enum values listed in
the API documentation for
[com.amazonaws.regions.Regions](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/Regions.html)

To initialize and seed the database with a sample scan, run `lein
reset-db`.

## Configuration

Configration is performed with Environment Variables. See
`resources/config.edn` for the exact names.

## Running it

### With docker-compose

Export your build environment variables:

1. Your Datomic Pro download credentials
    1. `export LEIN_USERNAME=...`
    1. `export LEIN_PASSWORD=...`

Then run `docker-compose build`.

Export your runtime environment variables:

1. Your AWS access key and secret key
    1. `export AWS_ACCESS_KEY=...`
    1. `export AWS_SECRET_KEY=...`
1. USPS_DATOMIC_URI
    1. "datomic:dev://datomic:4334/usps-processor" works well for running under docker-compose
1. USPS_SQS_REGION
    1. "US_WEST_2" or "US_EAST_1" or whatever region you want in all caps
1. USPS_SQS_QUEUE
    1. Name of the SQS queue to monitor for incoming scan data uploads from the USPS
1. USPS_SQS_FAIL_QUEUE
    1. Name of the queue to put failures on

Then run `docker-compose up`.  This will build the app, download and
run dependent containers, and start everything up.

NOTE: The Datomic image referenced in `docker-compose.yml` is a private image for internal use by
Democracy Works employees. If you want to use this you'll either need your own Datomic Pro license
or switch to Datomic Free.

### Running in CoreOS

The `script/build` and `script/deploy` scripts are designed to automate building and deploying to CoreOS.

1. Run `script/build`.
1. Note the resulting image name and push it if needed.
1. Set your FLEETCTL_TUNNEL env var to a node of the CoreOS cluster
   you want to deploy to.
1. Configure the following in Consul
   1. /aws/credentials/access-key
   2. /aws/credentials/secret-key
   3. /usps-processor/sqs/region
   4. /usps-processor/sqs/queue
   5. /usps-processor/sqs/fail-queue
   6. /usps-processor/datomic/uri
1. Make sure RabbitMQ and Datomic are running on CoreOS (with
   `fleetctl list-units`).
1. Run `script/deploy image/name`.


## Updating resources/zipcode-city-state.edn

To update resources/zipcode-city-state.edn, in the case that the USPS releases new data, download the new data from
http://www.usps.com/mailtracking/_csv/NonAutomated5Digit.csv and transform it into an edn map from integer zipcodes
to maps of the city and state. Note that some zipcodes appear multiple times. The existing file has made the decision
to take the first instance of a zipcode and remove all later ones.

## RabbitMQ events

usps-processor sends RabbitMQ messages to an "events" topic exchange. These
messages represent individual scans from the USPS (one scan on one mailing).

The message payloads are EDN-encoded Clojure maps that look like this:

```clojure
{:scan/facility-city-state {:city "Denver", :state "CO"}
 :scan/facility-zip "80211"
 :mailing/mailer-id-9 "123456789"
 :scan/service "040"
 :scan/time #inst "2015-02-24T00:00:00.000-00:00"
 :scan/barcode "000"
 :scan/operation-code "895"
 :mailing/serial-number-6 "654321"
 :mailing/serial-number-9 "789654321"
 :mailing/mailer-id-6 "123456"}
```


Copyright © 2014 Democracy Works, Inc.
