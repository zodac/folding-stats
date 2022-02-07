ARG JAVA_VERSION
ARG PROMETHEUS_VERSION

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

ARG PROMETHEUS_VERSION
FROM prom/prometheus:${PROMETHEUS_VERSION}

COPY --from=cert_builder /usr/local/certs/metrics/prometheus.crt /usr/local/certs/metrics/prometheus.crt
COPY --from=cert_builder /usr/local/certs/metrics/prometheus.key /usr/local/certs/metrics/prometheus.key

COPY ./docker/metrics/prometheus/config/prometheus.yml /etc/prometheus/prometheus.yml
COPY ./docker/metrics/prometheus/config/web-config.yml /etc/prometheus/web-config.yml

# Use root user to have permission on SSL files
USER root
