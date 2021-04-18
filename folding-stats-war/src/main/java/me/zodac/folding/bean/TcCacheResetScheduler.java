package me.zodac.folding.bean;

import me.zodac.folding.cache.tc.TcStatsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class TcCacheResetScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcCacheResetScheduler.class);

    @Schedule(dayOfMonth = "1", minute = "55", info = "Monthly cache reset for TC teams")
    public void monthlyTcStatsReset() {
        LOGGER.info("Resetting TC caches for new month");

        // TODO: [zodac] If we reset the cache but the system dies in between this and the next run, we cannot (currently)
        //   recalculate the initial state until we get an entry in for this month. Which would lead to the first update being lost.
        //   We should instead check that if no entry exists, rather than getting the first entry in this month, find the last entry
        //   in the previous month.
        //   It's a bit more work, and I don't really see the need for it unless we have a lot of failures. But if I get bored, it's
        //   something that could be valuable for resilience down the line.
        TcStatsCache.get().resetInitialCache();
        TcStatsCache.get().emptyCurrentCache();
    }
}
