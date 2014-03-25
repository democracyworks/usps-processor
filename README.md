# usps-processor

## Setup

You'll need a config.edn file available as a resource with the
following shape:

```clojure
{:aws {:creds {:access-key "your AWS access key"
               :secret-key "your AWS secret key"}
       :s3 {:bucket "S3-bucket-name-where-USPS-data-is-stored"}
       :sqs {:region #aws/region "AWS_region_enum" ; see below
             :queue "https://sqs.amazonaws.com/your-sqs-queue"
             :fail-queue "https://sqs.amazonaws.com/your-sqs-failure-queue"}
 :datomic {:uri "datomic://your-datomic-uri"
           :partition :usps-processor}}
```

The valid values for `:aws :sqs :region` are the enum values listed in
the API documentation for [com.amazonaws.regions.Regions](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/Regions.html)

Copyright © 2014 Democracy Works, Inc.
