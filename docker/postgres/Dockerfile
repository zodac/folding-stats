FROM postgres:13.2-alpine

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD

# These scripts should also be copied into the resource folder for PostgresDbManagerTest so they are tested before deployment
COPY ./docker/postgres/scripts/init/*.sql /docker-entrypoint-initdb.d/
RUN sed -i -e "s/ADMIN_USERNAME/${ADMIN_USERNAME}/g" /docker-entrypoint-initdb.d/admin-users.sql
RUN sed -i -e "s/ADMIN_PASSWORD/${ADMIN_PASSWORD}/g" /docker-entrypoint-initdb.d/admin-users.sql