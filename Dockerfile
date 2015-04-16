FROM clojure:lein-2.5.1

RUN mkdir -p /usr/src/usps-processor
WORKDIR /usr/src/usps-processor

COPY profiles.clj /usr/src/usps-processor/
COPY project.clj /usr/src/usps-processor/

RUN lein deps

COPY . /usr/src/usps-processor

RUN lein test
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" usps-processor-standalone.jar

CMD ["java", "-jar", "usps-processor-standalone.jar"]
