FROM quay.io/democracyworks/didor
MAINTAINER Democracy Works, Inc. <dev@democracy.works>

ADD project.clj /usps-processor/
WORKDIR /usps-processor

ADD lein_deps /usps-processor/

RUN bash /usps-processor/lein_deps

ADD ./ /usps-processor/

RUN rm /usps-processor/lein_deps

RUN lein test

EXPOSE 8080