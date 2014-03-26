FROM quay.io/democracyworks/clojure-api:latest
MAINTAINER TurboVote <dev@turbovote.org>

ONBUILD ADD ./resources/ /usps-processor/resources/

ADD ./target/usps-processor.jar /usps-processor/

ADD docker/start-usps-processor-importer.sh /start-usps-processor-importer.sh
ADD docker/supervisord-usps-processor-importer.conf /etc/supervisor/conf.d/supervisord-usps-processor-importer.conf
