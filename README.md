# Folding Stats

## Decisions

### Historic Stats

We're using a couple of queries to read the user stats from the `tc_user_stats` table, but not persisting daily/monthly
stats. If the historic pages are used quite a bit, this might be a bit slow, so there is a potential for
caching/persisting results. Could be persisted on a scheduled basis, or perhaps only persisted on the first call, then
cache those results. Need to do more profiling on a live system, see how much it is being used. A good idea to bring in
an ELK stack to instrument the system when we go live.

### How to extract Wildfly logs on container crash

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
    docker rm dummy