package me.zodac.folding.bean;

import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.cache.FoldingUserCache;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;


// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class InitialisationStartupBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialisationStartupBean.class);

    @PostConstruct
    public void init() {
        LOGGER.info("Started initialisation bean");

        loadDataIntoDb();
        initCaches();
    }

    private static void loadDataIntoDb() {
        LOGGER.warn("Adding initial data into DB (may fail if DB is already initialised)");

        addHardware();
        addFoldingUsers();
        addFoldingTeams();
        LOGGER.info("Initial data added to DB");
    }

    private static void initCaches() {
        LOGGER.debug("Initialising Folding user cache with DB data");
        final FoldingUserCache foldingUserCache = FoldingUserCache.getInstance();

        try {
            for (final FoldingUser foldingUser : PostgresDbManager.getAllFoldingUsers()) {
                foldingUserCache.add(foldingUser);
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising Folding user cache", e.getCause());
        }

        LOGGER.info("Caches initialised");
    }

    private static void addHardware() {
        final List<Hardware> allHardware = List.of(
                Hardware.createWithoutId(
                        "nVidia 1070", // ID 1
                        "nVidia 1070",
                        1.0D
                ),
                Hardware.createWithoutId(
                        "nVidia 3090", // ID 2
                        "nVidia 3090",
                        1.0D
                ),
                Hardware.createWithoutId(
                        "nVidia 1070", // ID 3
                        "nVidia 1070 (half)",
                        0.5D
                ),
                Hardware.createWithoutId(
                        "nVidia 3090", // ID 4
                        "nVidia 3090 (double)",
                        2.0D
                )
        );

        for (final Hardware hardware : allHardware) {
            try {
                PostgresDbManager.createHardware(hardware);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial hardware data", e.getCause());
            }
        }

        LOGGER.debug("Initial hardware added to DB");
    }

    private static void addFoldingUsers() {
        final List<FoldingUser> foldingUsers = List.of(
                FoldingUser.createWithoutId(
                        "BWG",
                        "BWG",
                        "8d10fbfda0813aa7288613e400484214",
                        1
                ),
                FoldingUser.createWithoutId(
                        "Bastiaan_NL",
                        "Bastiaan_NL",
                        "d1ed404fdb11570aa07d2294601ad292",
                        2
                ),
                FoldingUser.createWithoutId(
                        "BWG",
                        "BWG_With_Multiplier",
                        "8d10fbfda0813aa7288613e400484214",
                        3
                ),
                FoldingUser.createWithoutId(
                        "Bastiaan_NL",
                        "Bastiaan_NL_With_Multiplier",
                        "d1ed404fdb11570aa07d2294601ad292",
                        4
                )
        );

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                PostgresDbManager.createFoldingUser(foldingUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial Folding user data", e.getCause());
            }
        }

        LOGGER.debug("Initial Folding users added to DB");
    }

    private static void addFoldingTeams() {
        final List<FoldingTeam> foldingTeams = List.of(
                FoldingTeam.createWithoutId(
                        "Furry Folders",
                        2, // Bastiaan_NL
                        2,
                        FoldingTeam.EMPTY_POSITION,
                        FoldingTeam.EMPTY_POSITION
                ),
                FoldingTeam.createWithoutId(
                        "Freshly Waxed",
                        1, // BWG
                        1,
                        FoldingTeam.EMPTY_POSITION,
                        FoldingTeam.EMPTY_POSITION
                ),
                FoldingTeam.createWithoutId(
                        "Test",
                        3, // BWG_With_Multiplier
                        3,
                        4, // Bastiaan_NL_With_Multiplier
                        FoldingTeam.EMPTY_POSITION
                )
        );

        for (final FoldingTeam foldingTeam : foldingTeams) {
            try {
                PostgresDbManager.createFoldingTeam(foldingTeam);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial Folding team data", e.getCause());
            }
        }

        LOGGER.debug("Initial Folding teams added to DB");
    }
}
