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

Set up the Datomic credentials:

1. Copy `profiles.clj.sample` to `profiles.clj`
2. Edit in your datomic username and password.
3. Save it.

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
   1. /usps-processor/aws/credentials/access-key
   2. /usps-processor/aws/credentials/secret-key
   3. /usps-processor/sqs/region
   4. /usps-processor/sqs/queue
   5. /usps-processor/sqs/fail-queue
   6. /usps-processor/datomic/uri
1. Make sure RabbitMQ is running on CoreOS (with `fleetctl
   list-units`).
1. Run `script/deploy image/name`.

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

## Usage

To kick off processing a scan file, upload it to an S3 bucket that the AWS credentials you're using have read access to. Then send a message on the `USPS_SQS_QUEUE` that looks like this:

```clojure
{:filename "your-scan-file.txt" :bucket "s3-bucket"}
```

Check the logs for messages like "Processing s3-bucket / your-scan-file.txt" and
then later "Processed ... scans from s3-bucket / your-scan-file.txt".

Watch the `USPS_SQS_FAIL_QUEUE` for failure messages.

## Copyright

Copyright © 2014-2016 Democracy Works, Inc.
