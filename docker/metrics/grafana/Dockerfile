ARG GRAFANA_VERSION
FROM grafana/grafana-oss:${GRAFANA_VERSION}

COPY ./docker/metrics/grafana/config/custom.ini /opt/custom.ini

# Switch to root user to edit config
USER root
ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD
RUN sed -i -e "s|%ADMIN_USERNAME%|${ADMIN_USERNAME}|g" /opt/custom.ini && \
    sed -i -e "s|%ADMIN_PASSWORD%|${ADMIN_PASSWORD}|g" /opt/custom.ini

USER 472