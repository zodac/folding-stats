# Folding@Home Team Competition

The *Folding@Home Team Competition* is a competition where members of a [Folding@Home](https://foldingathome.org/) team can group together into
sub-teams to compete against each other, while still continuing to contribute to their parent team in the
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
        - [Offsetting User Stats](#offsetting-user-stats)
        - [Deleting Users](#deleting-users)
        - [Moving A User To Another Team](#moving-a-user-to-another-team)
- [Troubleshooting](#troubleshooting)
    - [Errors Performing Admin Functions](#errors-performing-admin-functions)
    - [Logging](#logging)
        - [Available Logs](#available-logs)
        - [Enable DEBUG Or TRACE Logs](#enabling-debug-or-trace-logs)
        - [Extracting Logs On Container Crash](#extracting-logs-on-container-crash)
    - [Backup And Restore The Database](#backup-and-restore-of-database)
- [Contributing](#contributing)
- [Contact Us](#contact-us)
- [License](#license)

# Overview

The idea is that sub-teams will be created with 'categories' of hardware, which will be filled by Folding@Home users. In order to uniquely identify a
piece of hardware for a user (while still allowing them to contribute under the same Folding@Home user name and team), we require each piece of
hardware to have its own [Folding@Home passkey](https://apps.foldingathome.org/getpasskey). Since it is possible to retrieve the points for a
user+passkey combination, this allows us to get the points for a single CPU or GPU.

## How Are Points Calculated

In order to try and normalise scores across different hardware, each hardware is assigned a multiplier, based on its average PPD. The best performing
GPU or CPU is used as the base and given a multiplier of **1.00**. All other hardware has their multiplier calculated using the formula, rounded to
two decimal places:

    Average PPD of best GPU / Average PPD of current GPU

Note that we will not compare CPUs to GPUs, but we *will* compare all makes of GPUs to other makes; nVidia and AMD GPUs, for example.

The average PPDs for hardware is retrieved from the [LARS PPD Database](https://folding.lar.systems/).

---

# Getting Started

If you're interested in running this for your own Folding@Home team, the instructions below should help you get set up.

## System Requirements

You'll need to have the following installed on your system:

- [git](https://git-scm.com/)
- [docker](https://www.docker.com/get-started)
- [docker-compose](https://docs.docker.com/compose/install/)

## Configuration

You can start by using `git clone` to copy the repository, then configuring the environment variables for your system. Copy the
existing `.env.template` file and rename it to `.env`. Then open it up and read the instructions, filling in the variables. By default:

- The competition runs from the 3rd of the month until the end of the month
- Both the backend and frontend will run using HTTPS
- The stats will be collected for all users and teams once an hour (at xx:55)
- At the end of the month:
    - The month's results are saved
    - All user's stats are reset to **0**
    - The hardware multipliers and average PPD are recalculated from LARS

The defaults should be sufficient for most users, but any variable in the first `Mandatory` section must be updated. These will be credentials, the
location of any SSL certificates needed, and the URLs to your own frontend.

## Starting The System

There are three components to the system:

- The `frontend`, hosting the UI
- The `backend`, for stats calculation and hardware/team/user management
- The `database`, for persistent storage

These are all run as individual docker containers, and are configured by the `docker-compose.yml` file in the root of the repository. To start, you
can run the commands:

    cd ~/<GIT_HOME>/folding-stats
    docker-compose up --build --detach

This will build and run the docker containers in the background, and you can then access the UI at [https://127.0.0.1](https://127.0.0.1).

## Adding Folding@Home Users To The System

Now that the system is running, we can begin adding users. However, we need to set up hardware and teams for each user to use.

This is done by opening the UI and clicking on the `System` -> `Admin` [tab](https://127.0.0.1/system) on the nav bar. Log in with the admin
credentials defined [above](#configuration). The sections below describe how to populate the system with users and teams.

### Adding Hardware

Click on the **List of Hardware** button. It should show an empty list, since we have no hardware yet. We don't need to manually add these, however.
Instead, we can click on the `Manual LARS Update` button. This will retrieve all available GPUs from the LARS database, and add them to the system. If
you refresh the page (it may need a **CTRL+F5** refresh), you should now be able to see the hardware in the **List of Hardware**
section.

#### Hardware Definition

A **hardware** has the following fields:

| Field Name      | Description                                                                                                    |
| --------------- | -------------------------------------------------------------------------------------------------------------- |
| _ID_            | System-generated ID for the hardware                                                                           |
| _Hardware Name_ | Unique hardware name as retrieved from the LARS DB                                                             |
| _Display Name_  | User-friendly name to be shown on the UI                                                                       |
| _Hardware Make_ | Manufacturer of the hardware (AMD, nVidia, etc)                                                                |
| _Hardware Type_ | The type of hardware (CPU, GPU, etc)                                                                           |
| _Multiplier_    | The multiplier for the hardware, comparing its average PPD to the best GPU or CPU, rounded to 2 decimal places |
| _Average PPD_   | The average PPD for the hardware, based on the value retrieved from the LARS DB                                |

Restrictions:

- _Hardware Name_ must be unique and cannot be empty
- _Display Name_ cannot be empty
- _Multiplier_ must be **1.00** or greater
- _Average PPD_ must be **1** or greater

#### LARS Hardware Update

We currently only support GPUs as hardware in the system, so we only retrieve the GPU data from LARS. CPU support may be introduced in future, but if
a CPU is needed, it can be manually added using the UI.

### Adding Teams

Click on the **List of Teams** button. It should also show an empty list. We can manually create a team (or many) by clicking on `Teams`
-> `Create Team`. You can now populate the fields and create a team.

Note that you do not *technically* add a user to a team. Instead, when you create a user, it is assigned to a team. As a result, the team admin screen
will not reference any users.

Please take a look at [User Categories](#user-categories) for an explanation on how we limit the size of a team.

#### Team Definition

A **team** has the following fields:

| Field Name         | Description                      |
| ------------------ | -------------------------------- |
| _ID_               | System-generated ID for the team |
| _Team Name_        | Unique team name                 |
| _Team Description_ | Description of the team          |
| _Forum Link_       | Link to the team's forum page    |

Restrictions:

- _Team Name_ must be unique and cannot be empty
- _Forum Link_ is optional, but if it is populated, it must be a valid URL

### Adding Users

As above, if you click on the **List of Users** button you will only see an empty list. But since we now have hardware and teams in the system, we can
begin adding users. Similar to the teams, simply click on `Users` -> `Create User` and populate the values. The team and hardware fields are
searchable, so no need to scroll through all available options.

#### User Definition

A **user** has the following fields:

| Field Name          | Description                                                                                                          |
| ------------------- | -------------------------------------------------------------------------------------------------------------------- |
| _ID_                | System-generated ID for the user                                                                                     |
| _Folding User Name_ | Folding@Home user name                                                                                               |
| _Display Name_      | Name to be displayed on the UI for the user (for example, if a user's forum name differs from Folding@Home user name |
| _Passkey_           | User's passkey **for this specific hardware**                                                                        |
| _Category_          | Category the user is competing under (see [User Categories](#user-categories) for more info)                         |
| _Profile Link_      | Link to the user's forum profile                                                                                     |
| _Live Stats Link_   | Link to the user's live stats link, if exposed through HFM, for example                                              |
| _Hardware_          | The hardware this user is competing under                                                                            |
| _Team_              | The team this user is competing under                                                                                |
| _Is Captain_        | Whether this user is the captain of the team                                                                         |

Restrictions:

- _Folding User Name_ can only include alphanumeric characters, or underscore (_), hyphen (-) or period (.)
- _Display Name_ cannot be empty
- _Passkey_ must be 32 characters long, and can only include alphanumeric characters
- The combination of _Folding User Name_ and _Passkey_ must be unique
- _Category_ must be valid according to [User Categories](#user-categories), and match the _Hardware Make_ and _Hardware Type_ of the user's hardware
- _Profile Link_ is optional, but if it is populated, it must be a valid URL
- _Live Stats Link_ is optional, but if it is populated, it must be a valid URL
- We expect at least 1 Work Unit to have been successfully completed by the _Folding User Name_ and _Passkey_, to confirm it is being used
- User cannot be added to a category that already has a maximum number of users
- User cannot be added to a team that already has the maximum number of users

Notes:

- If _Is Captain_ is selected but the user's team already has a captain, the old captain will be replaced as captain. "**There can be only one!**"
- There is no way to ensure a participant is actually only using their passkey on a single piece of hardware, so there is some level of trust
  involved. However, since we can view a user's stats on a per-hour basis, any suspicious passkey usage should be possible to find.

#### User Categories

The following user categories are available in the system:

| Category    | Description                                                                    | Default number of users per category | 
| ----------- | ------------------------------------------------------------------------------ | ------------------------------------ |
| AMD GPU     | Any hardware with _Hardware Make_ of **AMD** and _Hardware Type_ of **GPU**    | 1                                    |
| nVidia GPU  | Any hardware with _Hardware Make_ of **nVidia** and _Hardware Type_ of **GPU** | 1                                    |
| Wildcard    | Any hardware                                                                   | 1                                    |

Based on these limitations, the current maximum number of users for any team is **3**.

The number of users per category is configurable though [docker variables](#configuration).

However, adding new categories (like CPU-based categories, or more specific GPU categories) requires updating the *Category.java* class within the
source code. Please read the [Contributing](#Contributing) section for more information on how to do this.

## Adjustments During The Competition

### Offsetting User Stats

Sometimes a user's stats may need to be adjusted. Perhaps they were using the wrong hardware, or had their passkey on multiple GPUs by mistake. If for
any reason a user's points needs to be changed, simply click on `Users` -> `Edit User Points/Units` and enter the new wanted stats for the user.

### Deleting Users

When a user is deleted, we don't want the points they earned to be lost for their team. So whenever a user is deleted, their final stats until that
moment in the month are saved and retained for the team as "Retired User" stats. This retired user will remain until the end of the month (when the
stats are reset), at which point it will be removed from the team.

### Moving A User To Another Team

When a user is updated and has their team changed (meaning they have been moved), it is similar to if they have been [deleted](#deleting-users) from
their first team, and then added as a new user to their new team. So their old team will get a "Retired User" with the moved user's current stats, and
the new team will get the user with 0 stats.

---

# Troubleshooting

## Errors Performing Admin Functions

When performing any [admin functions](#adding-foldinghome-users-to-the-system), you may get a failure pop-up stating an operation failed. You can get
more information by viewing the console in your browser. In Google Chrome, this can be done by pressing **F12** and selecting the `Console` tab. You
may need to re-run the command to see the error after opening the console.

If this does not provide enough information, you can see the [Logging](#logging) section below on how to log into the `backend` container and view the
system log there.

## Logging

### Available Logs

The system currently has multiple logs available:

- server.log
    - This is the general application log, where most logging will be written to. It will also be printed to the console.
- audit.log
    - This is where all logging for *SecurityInterceptor.java* is written, detailing login attempts or access requests to WRITE operations. This is
      not printed to the console.

These can be accessed and viewed by:

- Connecting to the docker container and checking directory `/opt/jboss/wildfly/standalone/log`
- Attaching to the `backend_logs` volume (as described in [Extracting Logs On Container Crash](#extracting-logs-on-container-crash))
- Logging in through the Wildfly Admin UI

The logs are rotated each day, so you will see the following on the file system (with the non-timestamped log files referring to the current day's
logs):

    [root@backend log]# ls -1
    audit.log
    audit.log.2021-11-08
    server.log
    server.log.2021-11-08

### Enabling DEBUG Or TRACE Logs

This requires us to connect to the `backend` container, so we will need the container ID:

    $ docker container ls
    CONTAINER ID        IMAGE                    COMMAND                  CREATED             STATUS                   PORTS                                            NAMES
    c669fe278e36        folding-stats_backend    "/opt/jboss/wildfly/…"   3 minutes ago       Up 3 minutes             0.0.0.0:8080->8080/tcp, 0.0.0.0:9990->9990/tcp   backend
    334ace4ab4be        folding-stats_database   "docker-entrypoint.s…"   3 minutes ago       Up 3 minutes (healthy)   0.0.0.0:5432->5432/tcp                           database

Then connect to this container:

    $ docker exec -it c669fe278e36 bash
    [root@backend log]#

This gives us access to the backend container, which runs on the [Wildfly AS](https://www.wildfly.org/). Now we connect to the console:

    [root@backend jboss]# /opt/jboss/wildfly/bin/jboss-cli.sh --connect
    [standalone@localhost:9990 /]

Finally, we update both the logger for our code and the CONSOLE logger, otherwise the updated logs will be available in the log file only. Use
either `DEBUG` or `TRACE` as the *value* below:

    [standalone@localhost:9990 /] /subsystem=logging/logger=me.zodac.folding:write-attribute(name=level, value=DEBUG)
    {"outcome" => "success"}
    [standalone@localhost:9990 /] /subsystem=logging/console-handler=CONSOLE:write-attribute(name=level, value=DEBUG)
    {"outcome" => "success"}

### Extracting Logs On Container Crash

In the event of a container crash, the logs should be retained as we mount the `log` directory in a docker volume mount named `backend_logs`. However,
we cannot retrieve the file directly from the volume. Instead, we create a lightweight docker container, and attach the volume to this container. We
can then copy the file from the container to the host system.

For example, first check the available volumes:

    $ docker volume ls
    DRIVER VOLUME NAME
    local 97f3b514d34ebc85ebd71c61d1701b7faf585c2c755c62f78bea798b5a150c35
    local folding-stats_database_content
    local folding-stats_backend_logs

Then create a simple container, attaching the `folding-stats_backend_logs` volume (in read-only mode):

    docker container create --name dummy -v folding-stats_backend_logs:/root:ro folding-stats_backend

We can then copy the logs from the `dummy` container to our local machine:

    docker cp dummy:/root/server.log ./server.log
    docker cp dummy:/root/audit.log ./audit.log

And finally remove the `dummy` container:

    docker rm dummy

---

## Backup And Restore Of Database

In case of a migration to a new host, or a major issue where the docker volumes may be lost, we can create a backup of the database, then restore it
onto the new/remade environment. Please note that the instructions below are for the default `database` container, which runs on **PostgreSQL**.

To take a backup of the database, the following commands can be run against the `database` container:

    docker exec database pg_dump -U folding_user -F t folding_db -f export.tar
    docker cp database:/export.tar ~/export_$(date +%F).tar

The first line will create a backup of the DB in the `database` container, and the second will copy it out to the host.

Assuming a backup was previously created, it can be restored using the following commands against the `database` container:

    docker cp ~/export_<TIMESTAMP>.tar database:/export.tar
    docker exec database pg_restore -d folding_db export.tar -c -U folding_user

The first line will copy the backup from the host to the `database` container, and the second will restore the DB using the *export.tar* file.

---

# Contributing

Would you like to contribute? [Check here](CONTRIBUTING.md) for details on how.

---

# Contact Us

We are currently running the competition over at [ExtremeHW](https://forums.extremehw.net/forum/125-extreme-team-folding/), so you can get in touch
with us over there.

---

# License

The source code is released under the [MIT License](http://www.opensource.org/licenses/MIT).
