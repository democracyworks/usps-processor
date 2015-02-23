# usps-processor

## Setup

The valid values for `:aws :sqs :region` are the enum values listed in
the API documentation for [com.amazonaws.regions.Regions](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/Regions.html)

To initialize and seed the database with a sample scan, run `lein reset-db`.

## Running it

### In docker

Export your config environment variables:

1. Your AWS access key and secret key
    1. `export AWS_ACCESS_KEY=...`
    1. `export AWS_SECRET_KEY=...`
1. Your Datomic Pro download credentials
    1. `export LEIN_USERNAME=...`
    1. `export LEIN_PASSWORD=...`

Then run `docker-compose up`.
This will build the app, download and run dependent containers, and start everything up.

If you change anything in the app, rebuild it with `docker-compose build app` and then re-run
`docker-compose up`

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

> Immutant is currently disabled pending an upgrade to Immutant 2.

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


Copyright © 2014 Democracy Works, Inc.
