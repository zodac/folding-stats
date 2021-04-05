package me.zodac.folding.bean;

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

// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class ScheduledStatsParsing {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledStatsParsing.class);

    private final FoldingUsersCache foldingUsersCache = FoldingUsersCache.getInstance();

    @PostConstruct
    public void init() {
        LOGGER.info("Started stats parsing bean, running every hour");
    }

    // TODO: [zodac] Double check when Stanford actually update their stats, I think it's 5 minutes after the hour?
    @Schedule(hour = "*/1", minute = "10", info = "Every hour, 10 minutes after the hour")
    public void startStatsParsing() {
        LOGGER.info("Parsing Folding stats");

        final List<FoldingUser> usersToParse = foldingUsersCache.getAllUsers();

        if (usersToParse.isEmpty()) {
            LOGGER.warn("No Folding users configured in system!");
            return;
        }

        FoldingStatsParser.parseStats(usersToParse);
        LOGGER.info("Finished parsing");

        // TODO: [zodac] Stupid issue where my terminal won't print the last line of the docker console log
        //   Remove this eventually
        LOGGER.info("");
    }
}