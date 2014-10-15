# usps-processor

## Setup

You'll need a config.edn file available as a resource with the
following shape:

```clojure
{:aws {:creds {:access-key "your AWS access key"
               :secret-key "your AWS secret key"}
       :sqs {:region #aws/region "AWS_region_enum" ; see below
             :queue "your-sqs-queue"
             :fail-queue "your-sqs-failure-queue"}}
 :datomic {:uri "datomic://your-datomic-uri"
           :partition :usps-processor}
 :api {:port 8080}}
```

The valid values for `:aws :sqs :region` are the enum values listed in
the API documentation for [com.amazonaws.regions.Regions](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/Regions.html)

## Running it

To run the Importer:

```sh
lein run -m usps-processor.importer
```

To run the API:

```sh
lein run -m usps-processor.api
```

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
