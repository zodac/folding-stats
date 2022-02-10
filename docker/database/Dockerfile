ARG POSTGRES_VERSION
FROM postgres:${POSTGRES_VERSION}-alpine

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD
ARG READ_ONLY_USERNAME
ARG READ_ONLY_PASSWORD

# These scripts should also be copied into the resource folder for PostgresDbManagerTest so they are tested before deployment
COPY ./docker/database/scripts/init/*.sql /docker-entrypoint-initdb.d/
RUN sed -i -e "s/%ADMIN_USERNAME%/${ADMIN_USERNAME}/g" /docker-entrypoint-initdb.d/system-users.sql &&\
    sed -i -e "s/%ADMIN_PASSWORD%/${ADMIN_PASSWORD}/g" /docker-entrypoint-initdb.d/system-users.sql && \
    sed -i -e "s/%READ_ONLY_USERNAME%/${READ_ONLY_USERNAME}/g" /docker-entrypoint-initdb.d/system-users.sql && \
    sed -i -e "s/%READ_ONLY_PASSWORD%/${READ_ONLY_PASSWORD}/g" /docker-entrypoint-initdb.d/system-users.sql
