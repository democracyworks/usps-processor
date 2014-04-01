FROM quay.io/democracyworks/clojure-api-build:latest

ADD ./ /usps-processor/

# the WORKDIR will be added to the container as a volume by the build script
WORKDIR /usps-processor

VOLUME ["/servers/usps-processor/"]

ADD resources/immutant/usps-processor.clj /servers/usps-processor/

CMD ["script/build-ima"]
