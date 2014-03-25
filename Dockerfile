FROM quay.io/democracyworks/clojure-api:latest
MAINTAINER TurboVote <dev@turbovote.org>

ONBUILD ADD ./resources/ /var/local/usps-processor/resources/

ADD ./target/usps-processor.jar /var/local/usps-processor/
ADD docker/start-usps-processor.sh /start-usps-processor.sh
ADD docker/supervisord-usps-processor.conf /etc/supervisor/conf.d/supervisord-usps-processor.conf
