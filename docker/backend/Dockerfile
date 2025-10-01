# syntax = docker/dockerfile:1.2
ARG JAVA_VERSION
ARG MAVEN_VERSION
ARG DEBIAN_VERSION

############################
# Stage 1: Generate certs
############################
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS cert_builder

RUN mkdir --parents /usr/local/certs/stats/
WORKDIR /usr/local/certs/stats/

ARG KEYSTORE_PASSWORD
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# Generate PCKS12 keystore with self-signed certificate, and extract public crt and private key
RUN apt-get update && apt-get install -yqq --no-install-recommends openssl && \
    keytool \
        -genkey \
        -alias folding-stats \
        -dname "cn=folding,ou=stats,o=zodac.net,l=Auckland,s=Auckland,C=NZ" \
        -keyalg RSA \
        -keysize 4096 \
        -storetype PKCS12 \
        -keystore folding-stats.p12 \
        -validity 3650 \
        -storepass "${KEYSTORE_PASSWORD}" && \
    openssl pkcs12 -in folding-stats.p12 -out folding-stats.pem -nodes -passin pass:"${KEYSTORE_PASSWORD}" && \
    sed -n '/^-----BEGIN CERTIFICATE-----/,/^-----END CERTIFICATE-----/{p;/^-----END CERTIFICATE-----/q}' folding-stats.pem > folding-stats.crt && \
    sed -n '/^-----BEGIN PRIVATE KEY-----/,/^-----END PRIVATE KEY-----/{p;/^-----END PRIVATE KEY-----/q}' folding-stats.pem > folding-stats.key

############################
# Stage 2: Maven binary
############################
FROM maven:${MAVEN_VERSION}-eclipse-temurin-21 AS maven_builder

############################
# Stage 3: Build application
############################
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS application_builder

COPY --from=maven_builder /usr/share/maven/ /usr/share/maven/
ENV MAVEN_HOME="/usr/share/maven/"
ENV PATH="${PATH}:${MAVEN_HOME}/bin/"

RUN mkdir --parents /folding-stats-spring-boot
WORKDIR /folding-stats-spring-boot

# Root pom
COPY ./pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn install -P docker

# API module
COPY ./folding-stats-api/pom.xml ./folding-stats-api/
COPY ./folding-stats-api/src/main ./folding-stats-api/src/main
WORKDIR /folding-stats-spring-boot/folding-stats-api
RUN --mount=type=cache,target=/root/.m2 mvn install -P docker

# JAR module
WORKDIR /folding-stats-spring-boot
COPY ./folding-stats-jar/pom.xml ./folding-stats-jar/
COPY ./folding-stats-jar/src/main ./folding-stats-jar/src/main
WORKDIR /folding-stats-spring-boot/folding-stats-jar
RUN --mount=type=cache,target=/root/.m2 mvn install -P docker

# REST module
WORKDIR /folding-stats-spring-boot
COPY ./folding-stats-rest/pom.xml ./folding-stats-rest/pom.xml
COPY ./folding-stats-rest/src/main ./folding-stats-rest/src/main
WORKDIR /folding-stats-spring-boot/folding-stats-rest
RUN --mount=type=cache,target=/root/.m2 mvn verify -P docker -Dlogging-config-location="/var/backend/log4j2.xml" && \
    mv ./target/folding-stats-rest*.jar /folding-stats.jar

############################
# Stage 4: Custom JDK via jlink
############################
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS jdk_builder

RUN apt-get update && apt-get install -yqq --no-install-recommends binutils && \
        jlink --compress=2 \
        --no-header-files \
        --no-man-pages \
        --strip-debug \
        --add-modules java.se,jdk.crypto.cryptoki,jdk.management,jdk.unsupported \
        --output "/opt/jdk" && \
    strip -p --strip-unneeded "/opt/jdk/lib/server/libjvm.so"

############################
# Stage 5: Prep scripts
#############################
FROM debian:bookworm-slim AS prep_scripts
COPY ./docker/backend/scripts/startup.sh /startup.sh
COPY ./docker/backend/scripts/healthcheck.sh /healthcheck.sh
RUN chmod +x /startup.sh /healthcheck.sh

############################
# Stage 6: Install busybox
#############################
FROM debian:bookworm-slim AS shell_builder
RUN apt-get update && apt-get install -y busybox-static

############################
# Stage 7: Distroless runtime
############################
FROM gcr.io/distroless/base-debian${DEBIAN_VERSION}:nonroot AS runtime

# Copy custom JDK and app
COPY --from=jdk_builder /opt/jdk /opt/jdk
COPY --from=application_builder /folding-stats.jar /folding-stats.jar
# Certs
COPY --from=cert_builder /usr/local/certs/stats/ /usr/local/certs/stats/
# Config and startup script
COPY ./docker/backend/config/log4j2.xml /var/backend/log4j2.xml
#COPY ./docker/backend/scripts/startup.sh /startup.sh
COPY --from=prep_scripts /startup.sh /startup.sh
COPY --from=prep_scripts /healthcheck.sh /healthcheck.sh
COPY --from=shell_builder /bin/busybox /busybox

# Env
ENV JAVA_HOME="/opt/jdk"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /var/backend/logs

# Set up the healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 CMD ["/healthcheck.sh"]

ENTRYPOINT [ "/startup.sh" ]
