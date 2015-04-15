FROM quay.io/democracyworks/didor
MAINTAINER Democracy Works, Inc. <dev@democracy.works>

ADD project.clj /usps-processor/
WORKDIR /usps-processor
RUN lein deps

ADD ./ /usps-processor/

RUN lein test
RUN lein immutant war

EXPOSE 8080