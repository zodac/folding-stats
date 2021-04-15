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
        TcStatsCache.get().resetInitialCache();
        TcStatsCache.get().emptyCurrentCache();
    }
}
