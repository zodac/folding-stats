package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collections;
import java.util.List;

// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class ScheduledStatsParsing {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledStatsParsing.class);

    @EJB
    private StorageFacade storageFacade;

    @PostConstruct
    public void init() {
        LOGGER.info("Started stats parsing bean, running every hour");
    }

    @Schedule(hour = "*/1", minute = "15", info = "Every hour, 15 minutes after the hour")
    public void startStatsParsing() {
        LOGGER.info("Parsing Folding stats");


        final List<FoldingUser> usersToParse = getUsers();

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

    private List<FoldingUser> getUsers() {
        try {
            return storageFacade.getAllFoldingUsers();
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving Folding users", e.getCause());
            return Collections.emptyList();
        }
    }
}