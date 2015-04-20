FROM quay.io/democracyworks/didor:latest
MAINTAINER Democracy Works, Inc. <dev@democracy.works>

RUN mkdir -p /usps-processor
WORKDIR /usps-processor

COPY profiles.clj /usps-processor/
COPY project.clj /usps-processor/

RUN lein deps

COPY . /usps-processor

RUN lein test

RUN lein immutant war --name usps-processor --destination target

EXPOSE 8080