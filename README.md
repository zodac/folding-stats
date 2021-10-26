# README still a work in progress!

# Folding Stats

## Development

### Dependency Management

All dependencies can be updated using the `versions-maven-plugin`. By using the following command, all version will be
updated to the latest version available online:

    mvn versions:update-properties

### jOOQ Database Access

We use **jOOQ** for generating PostgreSQL queries (as seen in *PostgresDbManager.java*). We use **jOOQ** code generation
to generate files to make SQL query building simpler and able to conform to schemas. This requires a few steps when the
DB changes:

- Start the test containers (--> "Executing tests" <-- )
- Update the `docker/postgres/jooq/jooq-config.xml` file with the DB connection properties if not using the default
- Run the `generate.bat` batch file, which will generate the schemas (starting in a directory named `me`)
- Traverse the `me` directory until you get to `gen` (full path is `me/zodac/folding/db/postgres/gen`)
- Move the `gen` directory and all contents
  into `folding-stats/folding-stats-jar/src/main/java/me/zodac/folding/db/postgres`
    - Overwrite any existing files (or delete beforehand)

Once this is done, it will be possible to reference the DB tables/fields/schema from *PostgresDbManager.java* to assist
with SQL query generation.

### Adding Support for Another Database

Since the system is containerised, it is possible to swap out the default PostgreSQL DB for an alternative. The steps
required for this are:

- Update docker-compose.yml:
    - Remove the PostgreSQL DB `postgres` container
    - Add the new DB container (if containerised)
    - Update the `wildfly` container environment variables for "Database configuration"
- Add support for the new DB container in code:
    - Implement the *DbManager.java* interface, with code stored in
      the `folding-stats-jar/src/main/java/me/zodac/folding/db/<DB_NAME>` package
    - Update *DatabaseType.java* with an Enum for the new DB name
    - Update *DbManagerRetriever.java* with a new SWITCH condition for the DB name
    - Optionally, use the instructions in --> jooQ Database Access <-- to run jOOQ code generation for easier SQL query
      building

### JS/CSS Updates

The JS scripts and CSS stylesheets are stored in `docker/apache/`. They are then minified and merged by a maven plugin,
which creates the final files for the site. These CSS and JS files are generated in the `apache` docker container.

### REST RAML API

The REST endpoints are documented through RAML, with the source RAML file found in  `docker/apache/raml/api.raml`. This
is used to generate an HTML output which is saved to `docker/apache/site/api.html`, and can be accessed through the UI.
The RAML HTML is generated in the `apache` docker container.

## Decisions

### Historic Stats

We're using a couple of queries to read the user stats from the TC stats table, but not persisting hourly/daily/monthly
stats. This might be a bit slow if there is frequent access to the historic pages, so there is a potential for
caching/persisting results. Could be persisted on a scheduled basis, or perhaps only persisted on the first call, then
cache those results. Need to do more profiling on a live system, see how much it is being used. (A good idea to bring in
an ELK stack to instrument the system when we go live.)

## Executing Tests

TBC

## Executing linting

TBC

## Troubleshooting

### Take Backup Of Database

To take a backup of the database, the following commands can be run against the `postgres` container:

    docker exec postgres pg_dump -U folding_user -F t folding_db -f export.tar
    docker cp postgres:/export.tar ~/export_$(date +%F).tar

The first line will create a backup of the DB in the `postgres` container, and the second will copy it out to the host.

### Restore Database From Backup

Assuming a backup was previously created using the instructions in --> Take Backup Of Database <--, it can be restored
using the following commands against the `postgres` container:

    docker cp ~/export_<TIMESTAMP>.tar postgres:/export.tar
    docker exec postgres pg_restore -d folding_db export.tar -c -U folding_user

The first line will copy the backup from the host to the `postgres` container, and the second will restore the DB using
the *export.tar* file.

### Enabling DEBUG Logs

This requires us to connect to the `wildfly` container, so we will need the container ID:

    $ docker container ls
    CONTAINER ID        IMAGE                    COMMAND                  CREATED             STATUS                   PORTS                                            NAMES
    c669fe278e36        folding-stats_wildfly    "/opt/jboss/wildfly/…"   3 minutes ago       Up 3 minutes             0.0.0.0:8080->8080/tcp, 0.0.0.0:9990->9990/tcp   wildfly
    334ace4ab4be        folding-stats_postgres   "docker-entrypoint.s…"   3 minutes ago       Up 3 minutes (healthy)   0.0.0.0:5432->5432/tcp                           postgres

Then connect to this container (if using Linux, ignore the `winpty` at the start of the command):

    $ winpty docker exec -it c669fe278e36 bash
    [root@wildfly jboss]#

This gives us access to the Wildfly container. Now we connect to the console:

    [root@wildfly jboss]# ./wildfly/bin/jboss-cli.sh --connect
    [standalone@localhost:9990 /]

Finally, we update both the logger for our code, and the CONSOLE logger (otherwise the debug logs will be available in
the log file only):

    [standalone@localhost:9990 /] /subsystem=logging/logger=me.zodac.folding:write-attribute(name=level, value=DEBUG)
    {"outcome" => "success"}
    [standalone@localhost:9990 /] /subsystem=logging/console-handler=CONSOLE:write-attribute(name=level, value=DEBUG)
    {"outcome" => "success"}

## Logging

### Available Logs

The system currently has two logs available. Both can be viewed either through the Wildfly Admin UI, connecting to the
docker container and checking directory `/opt/jboss/wildfly/standalone/log`, or attaching to the `wildfly_logs` volume.

- server.log
    - This is the general application log, where most logging will be written to. It will also be printed to the
      console.
- audit.log
    - This is where all logging for *SecurityInterceptor.java* is written, detailing login attempts or access requests
      to WRITE operations. This is not printed to the console.

### How To Extract Wildfly Logs On Container Crash

A volume `wildfly_logs` should exist. We cannot retrieve the file directly from the volume. Instead, we create a
lightweight docker container, and attach the volume to this container. We can then copy the file from the container to
the host system.

For example, first check the available volumes:

    $ docker volume ls
    DRIVER VOLUME NAME
    local 97f3b514d34ebc85ebd71c61d1701b7faf585c2c755c62f78bea798b5a150c35
    local folding-stats_postgres_data
    local folding-stats_wildfly_logs

Then create a simple container, attaching the `folding-stats_wildfly_logs` volume (in read-only mode):

    docker container create --name dummy -v folding-stats_wildfly_logs:/root:ro folding-stats_wildfly
    docker cp dummy:/root/server.log ./server.log
    docker cp dummy:/root/audit.log ./audit.log
    docker rm dummy