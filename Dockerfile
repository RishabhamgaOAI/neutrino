FROM openjdk:17-alpine

RUN apk upgrade -U \
 && apk add curl \
 && rm -rf /var/cache/*

RUN apk add --no-cache nss
ARG JAR_FILE

COPY ${JAR_FILE} neutrino.jar
#COPY ./target/neutrino-0.0.1-SNAPSHOT.jar neutrino.jar
#COPY ./src/main/resources/moments.json /var/config/moments.json
COPY ./src/main/resources/cacerts /var/config/cacerts
#COPY ./target/neutrino-0.0.1-SNAPSHOT.jar neutrino.jar
#HEALTHCHECK --interval=30s --timeout=15s --retries=5 \
#CMD curl --silent --fail localhost:8081/actuator/health || exit 1


# New relic agent installation
RUN mkdir -p /usr/local/newrelic
ADD ./src/main/resources/newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./src/main/resources/newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml
ENV JAVA_OPTS="$JAVA_OPTS -javaagent:/usr/local/newrelic/newrelic.jar"

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -XX:InitialRAMPercentage=60 -XX:MaxRAMPercentage=75  -jar /neutrino.jar
