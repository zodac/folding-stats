# Developing folding-stats

- [Java](#java)
- [Docker](#docker)
    - [Docker Containers](#containers)
- [Database Management](#database-management)
    - [Adding Support for Another Database](#adding-support-for-another-database)
    - [Database Querying With jOOQ](#database-querying-with-jooq)
- [Tests](#tests)
    - [Available Tests](#available-tests)
        - [Unit Tests](#unit-tests)
        - [Integration Tests](#integration-tests)
        - [Performance Tests](#performance-tests)
            - [Hardware](#hardware)
        - [UI Tests](#ui-tests)
    - [Running Tests](#running-tests)
- [Linters](#linters)
    - [Available Linters](#available-linters)
        - [CheckStyle](#checkstyle)
        - [JavaDoc](#javadoc)
        - [License Check](#license-check)
        - [OWASP Dependency Check](#owasp-dependency-check)
        - [PiTest](#pitest)
        - [PMD](#pmd)
        - [SpotBugs](#spotbugs)
    - [Running Linters](#running-linters)
- [ProGuard Obfuscation](#proguard-obfuscation)
    - [Configuration](#configuration)

## Java

## Docker

### Containers

## Database Management

### Adding Support for Another Database

Since the system is containerised, it is possible to swap out the default PostgreSQL DB for an alternative. The steps required for this are:

- Update docker-compose.yml:
    - Remove the PostgreSQL DB `postgres` container
    - Add the new DB container (if containerised)
    - Update the `backend` container environment variables for "Database configuration"
- Add support for the new DB container in code:
    - Implement the *DbManager.java* interface, with code stored in the `folding-stats-jar/src/main/java/net/zodac/folding/db/<DB_NAME>` package
    - Update *DatabaseType.java* with an Enum for the new DB name
    - Update *DbManagerRetriever.java* with a new SWITCH condition for the DB name
    - Optionally, use the instructions in --> jooQ Database Access <-- to run jOOQ code generation for easier SQL query building

### Database Querying With jOOQ

We use **jOOQ** for generating PostgreSQL queries (as seen in *PostgresDbManager.java*). We use **jOOQ** code generation to generate files to make SQL
query building simpler and able to conform to schemas. This requires a few steps when the DB changes:

- Start the test containers (--> "Executing tests" <-- )
- Update the `docker/postgres/jooq/jooq-config.xml` file with the DB connection properties if not using the default
- Run the `generate.bat` batch file, which will generate the schemas (starting in a directory named `net`)
- Traverse the `net` directory until you get to `gen` (full path is `net/zodac/folding/db/postgres/gen`)
- Move the `gen` directory and all contents into `folding-stats/folding-stats-jar/src/main/java/net/zodac/folding/db/postgres`
    - Overwrite any existing files (or delete beforehand)

Once this is done, it will be possible to reference the DB tables/fields/schema from *PostgresDbManager.java* to help with SQL query generation.

## Tests

### Available Tests

#### Unit Tests

#### Integration Tests

#### Performance Tests

##### Hardware

| Test Case       | Number Of Users | Max Permitted Time |
|-----------------|-----------------|--------------------|
| GET All (empty) | 1               | 100ms              |

#### UI Tests

### Running Tests

## Linters

### Available Linters

#### CheckStyle

#### JavaDoc

#### License Check

#### OWASP Dependency Check

#### PiTest

#### PMD

#### SpotBugs

### Running Linters

## ProGuard Obfuscation

## Configuration
