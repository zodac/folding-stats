# syntax = docker/dockerfile:1.2
ARG JAVA_VERSION
ARG MAVEN_VERSION
ARG DEBIAN_VERSION

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS cert_builder

RUN mkdir --parents /usr/local/certs/stats/
WORKDIR /usr/local/certs/stats/

# Generate PCKS12 keystore with self-signed certificate, and extract public crt and private key
ARG KEYSTORE_PASSWORD
RUN apt-get update && apt-get install -yqq --no-install-recommends openssl && \
    keytool \
        -genkey \
        -alias folding-stats \
        -dname "cn=folding.zodac.me,ou=stats,o=zodac.me,l=Auckland,s=Auckland,C=NZ" \
        -keyalg RSA \
        -keysize 4096 \
        -storetype PKCS12 \
        -keystore folding-stats.p12 \
        -validity 3650 \
        -storepass "${KEYSTORE_PASSWORD}" && \
    openssl pkcs12 -in folding-stats.p12 -out folding-stats.pem -nodes -passin pass:"${KEYSTORE_PASSWORD}" && \
    cat folding-stats.pem | sed -n '/^-----BEGIN CERTIFICATE-----/,/^-----END CERTIFICATE-----/{p;/^-----END CERTIFICATE-----/q}' > folding-stats.crt && \
    cat folding-stats.pem | sed -n '/^-----BEGIN PRIVATE KEY-----/,/^-----END PRIVATE KEY-----/{p;/^-----END PRIVATE KEY-----/q}' > folding-stats.key

# Pulling maven image but only to retrieve the binary - we don't mind the JDK version here
FROM maven:${MAVEN_VERSION}-eclipse-temurin-21 AS maven_builder

ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS application_builder

COPY --from=maven_builder /usr/share/maven/ /usr/share/maven/

ENV MAVEN_HOME "/usr/share/maven/"
ENV PATH "${PATH}:${MAVEN_HOME}/bin/"

# Build folding-stats
RUN mkdir --parents /folding-stats-spring-boot

WORKDIR /folding-stats-spring-boot

# Copy source code
# Adding and building each module incrementally so we can retain some docker caches when building for tests
COPY ./pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn install -P docker

COPY ./folding-stats-api/pom.xml ./folding-stats-api/proguard-rules.cfg ./folding-stats-api/
COPY ./folding-stats-api/src/main ./folding-stats-api/src/main
RUN --mount=type=cache,target=/root/.m2 cd ./folding-stats-api && mvn install -P docker && cd ..

COPY ./folding-stats-jar/pom.xml ./folding-stats-jar/proguard-rules.cfg ./folding-stats-jar/
COPY ./folding-stats-jar/src/main ./folding-stats-jar/src/main
RUN --mount=type=cache,target=/root/.m2 cd ./folding-stats-jar && mvn install -P docker && cd ..

COPY ./folding-stats-rest/pom.xml ./folding-stats-rest/pom.xml
COPY ./folding-stats-rest/src/main ./folding-stats-rest/src/main

RUN --mount=type=cache,target=/root/.m2 cd ./folding-stats-rest && \
    mvn verify -P docker -Dlogging-config-location="/var/backend/log4j2.xml" && \
    mv ./target/folding-stats-rest*.jar /folding-stats.jar

ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS jdk_builder

RUN apt-get update && apt-get install -yqq --no-install-recommends binutils
RUN jlink --compress=2 \
    --no-header-files \
    --no-man-pages \
    --strip-debug \
    --add-modules java.se,jdk.crypto.cryptoki,jdk.management,jdk.unsupported \
    --output "/opt/jdk" && \
    strip -p --strip-unneeded "/opt/jdk/lib/server/libjvm.so"

FROM debian:${DEBIAN_VERSION}-slim AS spring_base

COPY --from=jdk_builder /opt/jdk /opt/jdk
COPY --from=application_builder /folding-stats.jar /folding-stats.jar
COPY ./docker/backend/scripts/startup.sh /startup.sh
COPY ./docker/backend/config/log4j2.xml /var/backend/log4j2.xml

ENV JAVA_HOME "/opt/jdk/"
ENV PATH "${PATH}:${JAVA_HOME}/bin"

# Updating system packages, and installing:
# - nano to allow user to edit log4j2.xml (smaller than vi)
# - wget to run healthcheck
RUN --mount=type=cache,target=/var/cache/apt apt-get update && apt-get upgrade -y && \
    apt-get install -yqq --no-install-recommends \
      nano \
      wget \
    && \
    apt-get autoremove && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy SSL certs
COPY --from=cert_builder /usr/local/certs/stats/folding-stats.p12 /usr/local/certs/stats/folding-stats.p12
COPY --from=cert_builder /usr/local/certs/stats/folding-stats.crt /usr/local/certs/stats/folding-stats.crt
COPY --from=cert_builder /usr/local/certs/stats/folding-stats.key /usr/local/certs/stats/folding-stats.key

# Create non-root user, make owner of log directory
RUN useradd -ms /bin/bash backend_user && \
    mkdir --parents /var/backend/logs && \
    chown -R backend_user:backend_user /var/backend/logs
USER backend_user

# Set default directory to the log directory
WORKDIR /var/backend/logs/

HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=10s \
            --retries=3 \
            CMD [ "/usr/bin/wget", "--no-check-certificate", "--no-verbose", "--tries=1", "--spider", "https://127.0.0.1:8443/folding/actuator/health" ]

ENTRYPOINT [ "/startup.sh" ]
