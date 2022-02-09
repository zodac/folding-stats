ARG DEBIAN_VERSION
ARG JAVA_VERSION
ARG PROMETHEUS_VERSION
ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD

FROM openjdk:${JAVA_VERSION}-jdk-slim AS cert_builder

RUN mkdir --parents /usr/local/certs/metrics
WORKDIR /usr/local/certs/metrics

# Generate self-signed certificate
RUN openssl \
    req \
    -x509 \
    -newkey rsa:4096 \
    -nodes \
    -keyout prometheus.key \
    -out prometheus.crt \
    -subj "/C=NZ/ST=Auckland/L=Auckland/O=zodac.me/OU=metrics/CN=folding.zodac.me"

FROM debian:${DEBIAN_VERSION}-slim AS password_hasher

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD
COPY ./docker/metrics/prometheus/config/web-config.yml /etc/prometheus/web-config.yml
RUN apt-get update && apt-get install -y apache2-utils && \
    sed -i -e "s/%ADMIN_USERNAME%/${ADMIN_USERNAME}/g" /etc/prometheus/web-config.yml && \
    sed -i -e "s/%ADMIN_PASSWORD%/$(htpasswd -bnBC 10 "" ${ADMIN_PASSWORD} | tr -d ':\n')/g" /etc/prometheus/web-config.yml && \
    echo "" >> /etc/prometheus/web-config.yml


ARG PROMETHEUS_VERSION
FROM prom/prometheus:${PROMETHEUS_VERSION}

COPY --from=cert_builder /usr/local/certs/metrics/prometheus.crt /usr/local/certs/metrics/prometheus.crt
COPY --from=cert_builder /usr/local/certs/metrics/prometheus.key /usr/local/certs/metrics/prometheus.key

COPY ./docker/metrics/prometheus/config/prometheus.yml /etc/prometheus/prometheus.yml
COPY --from=cert_builder /usr/local/certs/metrics/prometheus.key /usr/local/certs/metrics/prometheus.key
COPY --from=password_hasher /etc/prometheus/web-config.yml /etc/prometheus/web-config.yml

# Make the non-root user the owner of certs directory
USER root
RUN chown -R nobody:nobody /usr/local/certs/metrics/
USER nobody
