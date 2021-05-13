FROM postgres:13.2-alpine

# These scripts should also be copied into the resource folder for PostgresDbManagerTest so they are tested before deployment
COPY ./docker/postgres/scripts/init/*.sql /docker-entrypoint-initdb.d/