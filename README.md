# Folding@Home Team Competition

The *Folding@Home Team Competition* is a competition where members of a [Folding@Home](https://foldingathome.org/) team can group together into
subteams to compete against each other, while still continuing to contribute to their parent team in the
overall [Folding@Home donor statistics](https://stats.foldingathome.org/).

A live example of this competition can be seen running for the [ExtremeHW Folding@Home team](https://etf.axihub.ca/).

- [Overview](#overview)
    - [How Are Points Calculated](#how-are-points-calculated)
- [Getting Started](#getting-started)
    - [System Requirements](#system-requirements)
    - [Configuration](#configuration)
    - [Starting The System](#starting-the-system)
    - [Adding Folding@Home Users To The System](#adding-foldinghome-users-to-the-system)
        - [Adding Hardware](#adding-hardware)
            - [Hardware Definition](#hardware-definition)
            - [LARS Hardware Update](#lars-hardware-update)
        - [Adding Teams](#adding-teams)
            - [Team Definition](#team-definition)
        - [Adding Users](#adding-users)
            - [User Definition](#user-definition)
            - [User Categories](#user-categories)
    - [Adjustments During The Competition](#adjustments-during-the-competition)
        - [Requesting User Changes](#requesting-user-changes)
        - [Offsetting User Stats](#offsetting-user-stats)
        - [Deleting Users](#deleting-users)
        - [Moving A User To Another Team](#moving-a-user-to-another-team)
- [Troubleshooting](#troubleshooting)
    - [Supported Browsers](#supported-browsers)
    - [Containers](#containers)
        - [Checking Container Status](#checking-container-status)
        - [Restarting Containers](#restarting-containers)
    - [Errors Performing Admin Functions](#errors-performing-admin-functions)
    - [Logging](#logging)
        - [Available Logs](#available-logs)
        - [Changing Log Levels](#changing-log-levels)
        - [Extracting Logs On Container Crash](#extracting-logs-on-container-crash)
    - [Backup And Restore The Database](#backup-and-restore-of-database)
- [Contributing](#contributing)
- [Contact Us](#contact-us)
- [License](#license)

## Overview

The idea is that subteams will be created with 'categories' of hardware, which will be filled by Folding@Home users. In order to uniquely identify a
piece of hardware for a user (while still allowing them to contribute under the same Folding@Home username and team), we require each piece of
hardware to have its own [Folding@Home passkey](https://apps.foldingathome.org/getpasskey). Since it is possible to retrieve the points for a
user+passkey combination, this allows us to get the points for a single CPU or GPU.

## How Are Points Calculated

In order to try and normalise scores across different hardware, each hardware is assigned a multiplier, based on its average PPD. The best performing
GPU or CPU is used as the base and given a multiplier of **1.00**. All other hardware has their multiplier calculated using the formula, rounded to
two decimal places:

```text
Average PPD of best GPU / Average PPD of current GPU
```

Note that we will not compare CPUs to GPUs, but we *will* compare all makes of GPUs to other makes; nVidia and AMD GPUs, for example.

The average PPDs for hardware is retrieved from the [LARS PPD Database](https://folding.lar.systems/).

----

## Getting Started

If you're interested in running this for your own Folding@Home team, the instructions below should help you get set up.

### System Requirements

You'll need to have the following installed on your system:

- [git](https://git-scm.com/)
- [docker](https://www.docker.com/get-started)

### Configuration

You can start by using `git clone` to copy the repository, then configuring the environment variables for your system. Copy the
existing `.env.template` file and rename it to `.env`. Then open it up and read the instructions, filling in the variables. Using the default values:

- The competition runs from the 3rd of the month until the end of the month
- Both the backend and frontend will run using HTTPS
- The stats will be collected for all users and teams once an hour (at xx:55)
- At the end of the month:
    - The month's results are saved
    - All user's stats are reset to **0**
    - The hardware multipliers and average PPD are recalculated from LARS

The defaults should be enough for most users, but any variable in the first `Mandatory` section must be updated. These will be credentials, the
location of any SSL certificates needed, and the URLs to your own frontend.

### Starting The System

There are four components to the system:

- The `frontend` container, hosting the UI
- The `backend` container, for stats calculation and hardware/team/user management
- The `database` container, for persistent storage

These are all run as individual docker containers, and are configured by the `docker-compose.yml` file in the root of the repository. To start, you
can run the commands:

```bash
cd ~/<GIT_HOME>/folding-stats
docker-compose up --build --detach
```

This will build and run the docker containers in the background. It will take a minute or so for the backend to come online. You
can [check the status of the containers](#checking-container-status) to see if they are online.

Once all containers have a `STATUS` that is **healthy**, you can then access the UI at [https://127.0.0.1](https://127.0.0.1).

### Adding Folding@Home Users To The System

Now that the system is running, we can begin adding users. However, we need to set up hardware and teams for each user to use.

This is done by opening the UI and clicking on the `System` -> `Admin` [tab](https://127.0.0.1/system) on the nav bar. Log in with the admin
credentials defined [above](#configuration). The sections below describe how to populate the system with users and teams.

#### Adding Hardware

Click on the **List of Hardware** button. It should show an empty list, since we have no hardware yet. We don't need to manually add these, however.
Instead, we can click on the `Manual LARS Update` button. This will retrieve all available GPUs from the LARS database, and add them to the system. If
you refresh the page (it may need a **CTRL+F5** refresh), you should now be able to see the hardware in the **List of Hardware**
section.

##### Hardware Definition

A **hardware** has the following fields:

| Field Name      | Description                                                                                                    |
|-----------------|----------------------------------------------------------------------------------------------------------------|
| *ID*            | System-generated ID for the hardware                                                                           |
| *Hardware Name* | Unique hardware name as retrieved from the LARS DB                                                             |
| *Display Name*  | User-friendly name to be shown on the UI                                                                       |
| *Hardware Make* | Manufacturer of the hardware (AMD, nVidia, etc)                                                                |
| *Hardware Type* | The type of hardware (CPU, GPU, etc)                                                                           |
| *Multiplier*    | The multiplier for the hardware, comparing its average PPD to the best GPU or CPU, rounded to 2 decimal places |
| *Average PPD*   | The average PPD for the hardware, based on the value retrieved from the LARS DB                                |

Restrictions:

- *Hardware Name* must be unique and cannot be empty
- *Display Name* cannot be empty
- *Multiplier* must be **1.00** or greater
- *Average PPD* must be **1** or greater

##### LARS Hardware Update

We currently only support GPUs as hardware in the system, so we only retrieve the GPU data from LARS. CPU support may be introduced in the future, but
if a CPU is needed, it can be manually added using the UI.

#### Adding Teams

Click on the **List of Teams** button. It should also show an empty list. We can manually create a team (or many) by clicking on `Teams`
-> `Create Team`. You can now populate the fields and create a team.

Note that you do not *technically* add a user to a team. Instead, when you create a user, it is assigned to a team. As a result, the team admin screen
will not reference any users.

Please take a look at [User Categories](#user-categories) for an explanation on how we limit the size of a team.

##### Team Definition

A **team** has the following fields:

| Field Name         | Description                      |
|--------------------|----------------------------------|
| *ID*               | System-generated ID for the team |
| *Team Name*        | Unique team name                 |
| *Team Description* | Description of the team          |
| *Forum Link*       | Link to the team's forum page    |

Restrictions:

- *Team Name* must be unique and cannot be empty
- *Forum Link* is optional, but if it is populated, it must be a valid URL

#### Adding Users

As above, if you click on the **List of Users** button you will only see an empty list. But since we now have hardware and teams in the system, we can
begin adding users. Similar to the teams, simply click on `Users` -> `Create User` and populate the values. The team and hardware fields are
searchable, so no need to scroll through all available options.

##### User Definition

A **user** has the following fields:

| Field Name         | Description                                                                                                          |
|--------------------|----------------------------------------------------------------------------------------------------------------------|
| *ID*               | System-generated ID for the user                                                                                     |
| *Folding UserName* | Folding@Home user name                                                                                               |
| *Display Name*     | Name to be displayed on the UI for the user (for example, if a user's forum name differs from Folding@Home user name |
| *Passkey*          | User's passkey **for this specific hardware**                                                                        |
| *Category*         | Category the user is competing under (see [User Categories](#user-categories) for more info)                         |
| *Profile Link*     | Link to the user's forum profile                                                                                     |
| *Live Stats Link*  | Link to the user's live stats link, if exposed through HFM, for example                                              |
| *Hardware*         | The hardware this user is competing under                                                                            |
| *Team*             | The team this user is competing under                                                                                |
| *Is Captain*       | Whether this user is the captain of the team                                                                         |

Restrictions:

- *Folding UserName* can only include alphanumeric characters, or underscore (_), hyphen (-) or period (.)
- *Display Name* cannot be empty
- *Passkey* must be 32 characters long, and can only include alphanumeric characters
- The combination of *Folding UserName* and *Passkey* must be unique
- *Category* must be valid according to [User Categories](#user-categories), and match the *Hardware Make* and *Hardware Type* of the user's hardware
- *Profile Link* is optional, but if it is populated, it must be a valid URL
- *Live Stats Link* is optional, but if it is populated, it must be a valid URL
- We expect at least 1 Work Unit to have been successfully completed by the *Folding UserName* and *Passkey*, to confirm it is being used
- User cannot be added to a category that already has a maximum number of users
- User cannot be added to a team that already has the maximum number of users

Notes:

- If *Is Captain* is selected but the user's team already has a captain, the old captain will be replaced as captain. "**There can be only one!**"
- There is no way to ensure a participant is actually only using their passkey on a single piece of hardware, so there is some level of trust
  involved. However, since we can view a user's stats per-hourly, any suspicious passkey usage should be possible to find.

##### User Categories

The following user categories are available in the system:

| Category   | Description                                                                    | Default number of users per category |
|------------|--------------------------------------------------------------------------------|--------------------------------------|
| AMD GPU    | Any hardware with *Hardware Make* of **AMD** and *Hardware Type* of **GPU**    | 1                                    |
| nVidia GPU | Any hardware with *Hardware Make* of **nVidia** and *Hardware Type* of **GPU** | 1                                    |
| Wildcard   | Any hardware                                                                   | 1                                    |

Based on these limitations, the current maximum number of users for any team is **3**.

The number of users per category is configurable though [docker variables](#configuration).

However, adding new categories (like CPU-based categories, or more specific GPU categories) requires updating
the [Category.java](./folding-stats-api/src/main/java/net/zodac/folding/api/tc/Category.java) class within the source code. Please read
the [Contributing](#contributing) section for more information on how to do this.

### Adjustments During The Competition

#### Requesting User Changes

A user may request some of their data to be changed, reducing the load on the administrator. They can make a request to change:

- Folding@Home Username
- Folding@Home passkey
- Live Folding@Home stats link
- Hardware used

These changes can be requested to be made immediately, or deferred to the next month. The admin may then reject or approve these requests (and can
override when they should be applied).

Any other changes (like changing the team a user is competing for, for example) must be done by the admin manually.

#### Offsetting User Stats

Sometimes a user's stats may need to be adjusted. Perhaps they were using the wrong hardware, or had their passkey on multiple GPUs by mistake. If for
any reason a user's points needs to be changed, simply click on `Users`
-> `Edit User Points/Units` and enter the new wanted stats for the user.

#### Deleting Users

When a user is deleted, we don't want the points they earned to be lost for their team. So whenever a user is deleted, their final stats until that
moment in the month are saved and retained for the team as "Retired User" stats. This retired user will remain until the end of the month (when the
stats are reset), at which point it will be removed from the team.

#### Moving A User To Another Team

When a user is updated and has their team changed (meaning they have been moved), it is similar to if they have been [deleted](#deleting-users) from
their first team, and then added as a new user to their new team. So their old team will get a "Retired User" with the moved user's current stats, and
the new team will get the user with 0 stats.

----

## Troubleshooting

### Supported Browsers

Currently, the UI is only tested against the latest version of Google Chrome. Hopefully this will be expanded, but for now if you see any issues,
please try again using Google Chrome.

### Containers

#### Checking Container Status

To check the status of any containers, the following command can be used:

```bash
docker ps -a
```

This will show any docker containers (running and stopped) on the system and their status. When the system first comes online, you should see the
following:

```bash
CONTAINER ID   IMAGE                      COMMAND                  CREATED                  STATUS                            PORTS                                           NAMES
6d314cbbb902   folding-stats_frontend     "httpd-foreground"       Less than a second ago   Up Less than a second             80/tcp, 0.0.0.0:443->443/tcp, :::443->443/tcp   frontend
a7b722f8a178   folding-stats_backend      "/startup.sh"            2 seconds ago            Up 1 second (health: starting)    0.0.0.0:8443->8443/tcp, :::8443->8443/tcp       backend
312f5f61ec87   folding-stats_database     "docker-entrypoint.s…"   3 seconds ago            Up 3 seconds (health: starting)   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp       database
```

(Note that your `CONTAINER ID` value will be different.)

Pay attention to the `STATUS` value. When the system first comes online, the `backend` and `database` containers will take a minute or two to start
up, as seen by the value **health: starting**. Once they are successfully online, the `STATUS` will change to:

```bash
CONTAINER ID   IMAGE                    COMMAND                  CREATED         STATUS                   PORTS                                           NAMES
7092e5a354eb   folding-stats_frontend     "httpd-foreground"       17 hours ago   Up 17 hours             80/tcp, 0.0.0.0:443->443/tcp, :::443->443/tcp   frontend
6d2128137d5a   folding-stats_backend      "/startup.sh"            17 hours ago   Up 17 hours (healthy)   0.0.0.0:8443->8443/tcp, :::8443->8443/tcp       backend
e713645e6a43   folding-stats_database     "docker-entrypoint.s…"   17 hours ago   Up 17 hours (healthy)   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp       database
```

However, if one or more of the containers has stopped, you may see a container marked as **Exited**:

```bash
CONTAINER ID   IMAGE                        COMMAND                  CREATED         STATUS                             PORTS                                           NAMES
7092e5a354eb   folding-stats_frontend     "httpd-foreground"       17 hours ago   Up 17 hours                           80/tcp, 0.0.0.0:443->443/tcp, :::443->443/tcp   frontend
6d2128137d5a   folding-stats_backend      "/startup.sh"            17 hours ago   Up 17 hours (healthy)                 0.0.0.0:8443->8443/tcp, :::8443->8443/tcp       backend
e713645e6a43   folding-stats_database     "docker-entrypoint.s…"   17 hours ago   Up 17 hours (healthy)                 0.0.0.0:5432->5432/tcp, :::5432->5432/tcp       database
```

#### Restarting Containers

While it is possible to restart a single container if one has gone down, it is simpler to just restart the entire docker-compose set up. Since we save
the database content in a docker volume, any stats will be retained through a restart.

To stop any remaining containers, execute the following command (in the `folding-stats` root directory):

```bash
docker compose down
```

And to bring the containers back online, execute the command:

```bash
docker compose up --build --detach
```

----

### Errors Performing Admin Functions

When performing any [admin functions](#adding-foldinghome-users-to-the-system), you may get a failure pop-up stating an operation failed. You can get
more information by viewing the console in your browser. In Google Chrome, this can be done by pressing **F12** and selecting the `Console` tab. You
may need to re-run the command to see the error after opening the console.

If this does not provide enough information, you can see the [Logging](#logging) section below on how to log into the `backend` container and view the
system log there.

### Logging

#### Available Logs

The system currently has multiple logs available:

| Log File Name  | Description                                                                                                                                                                                                                         | Log Level | Printed To Console? |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|---------------------|
| *audit.log*    | This log is for REST requests on the system. Input payloads will be logged here.                                                                                                                                                    | INFO      | No                  |
| *lars.log*     | Used to log monthly updates to hardware based on the LARS PPD DB                                                                                                                                                                    | DEBUG     | No                  |
| *security.log* | This is where all logging for [SecurityInterceptor.java](./folding-stats-rest/src/main/java/net/zodac/folding/rest/interceptor/SecurityInterceptor.java) is written. Details login attempts or access requests to WRITE operations. | DEBUG     | No                  |
| *server.log*   | This is the general application log, where most logging will be written to.                                                                                                                                                         | INFO      | Yes                 |
| *sql.log*      | This is used to log all SQL queries made to the DB.                                                                                                                                                                                 | INFO      | No                  |
| *stats.log*    | This is used to log user stats retrieval.                                                                                                                                                                                           | DEBUG     | No                  |

These can be accessed and viewed by:

- Connecting to the `backend` docker container and checking directory `/var/backend/logs`
- Attaching to the `backend_logs` volume, as described in [Extracting Logs On Container Crash](#extracting-logs-on-container-crash)

The logs are rotated each day, where each previous day's logs will be zipped and saved in the same directory.

#### Changing Log Levels

For additional information to debug issues, we can change the log level from **INFO** to **DEBUG** or **TRACE**. This requires us to connect to
the `backend` container:

```bash
docker exec -it backend bash
```

We can now edit the */var/backend/log4j2.xml.xml* configuration for our logging. You can use the available `vi` command:

```bash
vi /var/backend/log4j2.xml
```

We can change the log level printed to the server.log and console by updating this line:

```xml
<root level="INFO">
    <Appender-ref ref="SERVER_LOG"/>
    <Appender-ref ref="CONSOLE"/>
</root>
```

To:

```xml
<root level="DEBUG">
    <Appender-ref ref="SERVER_LOG"/>
    <Appender-ref ref="CONSOLE"/>
</root>
```

Save and exit the `vi` editor. After 60 seconds, re-run the failing use-case and the log level will be changed. Remember to reset the log level back
to **INFO** when finished to avoid the logs getting too large.

Also note, that for those more familiar with logback, additional loggers are defined here and can also have their log levels changes. And specific
packages can have their log levels explicitly, of which there are some examples already.

### Extracting Logs On Container Crash

In case of a container crash, the logs should be retained as we mount the `logs` directory in a docker volume mount named `backend_logs`.
However, we cannot retrieve the file directly from the volume. Instead, we create a lightweight docker container, and attach the volume to this
container. We can then copy the file from the container to the host system.

For example, first check the available volumes:

```bash
$ docker volume ls
DRIVER    VOLUME NAME
local     folding-stats_backend_certs
local     folding-stats_backend_logs
local     folding-stats_database_content
```

Then create a simple container, attaching the `folding-stats_backend_logs` volume (in read-only mode):

```bash
docker container create --name dummy -v folding-stats_backend_logs:/root:ro folding-stats_backend
```

We can then copy the logs from the `dummy` container to our local machine:

```bash
docker cp dummy:/root/audit.log ./audit.log
docker cp dummy:/root/lars.log ./lars.log
docker cp dummy:/root/security.log ./security.log
docker cp dummy:/root/server.log ./server.log
docker cp dummy:/root/sql.log ./sql.log
docker cp dummy:/root/stats.log ./stats.log
```

And finally remove the `dummy` container:

```bash
docker rm dummy
```

----

### Backup And Restore Of Database

In case of a migration to a new host, or a major issue where the docker volumes may be lost, we can create a backup of the database, then restore it
onto the new/remade environment. Please note that the instructions below are for the default `database` container, which runs on **PostgreSQL**.

To take a backup of the database, the following commands can be run against the `database` container:

```bash
docker exec database pg_dump -U folding_user -F t folding_db -f export.tar
docker cp database:/export.tar ~/export_$(date +%F).tar
```

The first line will create a backup of the DB in the `database` container, and the second will copy it out to the host.

Assuming a backup was previously created, it can be restored using the following commands against the `database`
container:

```bash
docker cp ~/export_<TIMESTAMP>.tar database:/export.tar
docker exec database pg_restore -d folding_db export.tar -c -U folding_user
```

The first line will copy the backup from the host to the `database` container, and the second will restore the DB using the *export.tar* file.

----

## Contributing

Would you like to contribute? [Check here](./CONTRIBUTING.md) for details on how.

----

## Contact Us

We are currently running the competition over at [ExtremeHW](https://extremehw.net/forum/125-extreme-team-folding/), so you can get in touch
with us over there.

----

## License

The source code is released under the [BSD Zero Clause License](https://opensource.org/licenses/0BSD).
