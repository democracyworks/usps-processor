# usps-processor

## Setup

You'll need a config.edn file available as a resource. See
`dev-resources/config.edn.sample` for guidance.

The valid values for `:aws :sqs :region` are the enum values listed in
the API documentation for [com.amazonaws.regions.Regions](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/Regions.html)

To initialize and seed the database with a sample scan, run `lein reset-db`.

## Running it

### In docker

Create a `docker-dev-resources/config.edn` file based on the sample
file there. The `datomic` value is appropriate for running inside
Docker.

1. Set up [immutant-docker](https://github.com/turbovote/immutant-docker)
as specified in its README.
2. `./script/docker-run`

### Standalone

To run the Importer:

```sh
lein run -m usps-processor.importer
```

To run the API:

```sh
lein run -m usps-processor.api
```

### Immutant

To run both in Immutant:

```sh
lein immutant deploy
lein immutant run
```

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
