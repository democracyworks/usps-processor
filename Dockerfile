FROM clojure:lein-2.7.1-alpine
MAINTAINER Democracy Works, Inc. <dev@democracy.works>

RUN mkdir -p /usps-processor
WORKDIR /usps-processor

COPY profiles.clj /usps-processor/
COPY project.clj /usps-processor/

ARG env=production
ARG DATOMIC_USERNAME
ARG DATOMIC_PASSWORD

RUN lein with-profiles $env,datomic-repo deps

COPY . /usps-processor

RUN lein with-profiles $env,datomic-repo,test test

RUN lein with-profiles $env,datomic-repo uberjar

CMD ["java", "-javaagent:resources/jars/com.newrelic.agent.java/newrelic-agent.jar", "-jar", "target/usps-processor.jar"]
