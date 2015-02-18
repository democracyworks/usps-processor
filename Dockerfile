FROM quay.io/democracyworks/clojure-api:latest
MAINTAINER Democracy Works, Inc. <dev@democracy.works>

ADD ./ /usps-processor/

WORKDIR /usps-processor

VOLUME ["/servers/usps-processor/"]

ADD resources/immutant/usps-processor.clj /servers/usps-processor/

CMD ["sh", "-c", "lein with-profile $ENVIRONMENT run"]
