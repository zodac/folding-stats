package me.zodac.folding.rest;

import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.cache.FoldingUsersCache;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;

@Startup
@Singleton
public class SchedulesStatsParsing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulesStatsParsing.class);

    private final FoldingUsersCache foldingUsersCache = FoldingUsersCache.getInstance();

    @PostConstruct
    public void init() {
        LOGGER.info("Started stats parsing bean, running every hour");
    }

    // TODO: [zodac] Double check when Stanford actually update their stats, I think it's 5 mins after the hour?
    @Schedule(hour = "*/1", minute = "10", info = "Every hour, 10 minutes after the hour")
    public void startStatsParsing() {
        LOGGER.info("Parsing Folding stats");

        final List<FoldingUser> usersToParse = foldingUsersCache.getAllUsers();

        if (usersToParse.isEmpty()) {
            LOGGER.warn("No users configured in system!");
            return;
        }

        FoldingStatsParser.parseStats(usersToParse);
		LOGGER.info("Finished parsing");
		LOGGER.info("");
    }
}