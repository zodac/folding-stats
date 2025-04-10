ARG POSTGRES_VERSION
FROM postgres:${POSTGRES_VERSION}-alpine

# Updating system packages
RUN apk --no-cache update && apk --no-cache upgrade

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD
ARG READ_ONLY_USERNAME
ARG READ_ONLY_PASSWORD

# These scripts should also be copied into the resource folder for PostgresDbManagerTest so they are tested before deployment
COPY ./docker/database/scripts/init/*.sql /docker-entrypoint-initdb.d/
RUN sed -i -e "s|%ADMIN_USERNAME%|${ADMIN_USERNAME}|g" /docker-entrypoint-initdb.d/system-users.sql \
    -e "s|%ADMIN_PASSWORD%|${ADMIN_PASSWORD}|g" /docker-entrypoint-initdb.d/system-users.sql \
    -e "s|%READ_ONLY_USERNAME%|${READ_ONLY_USERNAME}|g" /docker-entrypoint-initdb.d/system-users.sql \
    -e "s|%READ_ONLY_PASSWORD%|${READ_ONLY_PASSWORD}|g" /docker-entrypoint-initdb.d/system-users.sql

HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=10s \
            --retries=3 \
            CMD [ "pg_isready", "--host", "localhost" ]
