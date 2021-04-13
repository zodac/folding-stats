package me.zodac.folding.bean;

import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.cache.FoldingTeamCache;
import me.zodac.folding.cache.FoldingUserCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.tc.TcStatsCache;
import me.zodac.folding.db.DbManagerRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;


// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    private final DbManager dbManager = DbManagerRetriever.get();

    @EJB
    private ScheduledStatsParser scheduledStatsParser;

    @PostConstruct
    public void init() {
        LOGGER.info("Started initialisation bean");

        loadDataIntoDb(); // TODO: [zodac] Remove this eventually
        initPojoCaches();
        initTcStatsCache(); // TODO: [zodac] Add bean to reset initial points cache at start of month
    }

    private void initTcStatsCache() {
        try {
            if (!dbManager.doTcStatsExist()) {
                LOGGER.warn("No TC stats data exists in the DB");
                scheduledStatsParser.startStatsParsing(); // TODO: [zodac] Remove this eventually
                return;
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to check DB state", e.getCause());
        }


        final List<FoldingUser> foldingUsers = FoldingUserCache.get().getAll();
        final Month currentMonth = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().getMonth();

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                final UserStats initialStatsForUser = dbManager.getFirstPointsForUserInMonth(foldingUser, currentMonth);
                LOGGER.info("Found initial stats for {} for user {}: {}", currentMonth, foldingUser, initialStatsForUser);
                TcStatsCache.get().addInitialStats(foldingUser.getId(), initialStatsForUser);
            } catch (final NotFoundException e) {
                LOGGER.debug("No initial stats in DB for {}", foldingUser, e);
                LOGGER.warn("No initial stats in DB for {}", foldingUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get initial stats for {} for user {}", currentMonth, foldingUser, e.getCause());
            }

            try {
                final UserStats currentStatsForUser = dbManager.getCurrentPointsForUserInMonth(foldingUser, currentMonth);
                LOGGER.info("Found current stats for {} for user {}: {}", currentMonth, foldingUser, currentStatsForUser);
                TcStatsCache.get().addCurrentStats(foldingUser.getId(), currentStatsForUser);
            } catch (final NotFoundException e) {
                LOGGER.debug("No current stats in DB for {}", foldingUser, e);
                LOGGER.warn("No current stats in DB for {}", foldingUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get current stats for {} for user {}", currentMonth, foldingUser, e.getCause());
            }
        }
    }

    private void loadDataIntoDb() {
        try {
            if (!dbManager.getAllHardware().isEmpty()) {
                LOGGER.warn("Initial data already exists in DB");
                return;
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to check DB state", e.getCause());
        }

        LOGGER.debug("Adding initial data into DB");
        addHardware();
        addFoldingUsers();
        addFoldingTeams();
        LOGGER.info("Initial data added to DB");
    }

    private void initPojoCaches() {
        try {
            LOGGER.debug("Initialising hardware cache with DB data");
            HardwareCache.get().addAll(dbManager.getAllHardware());
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising hardware cache", e.getCause());
        }

        try {
            LOGGER.debug("Initialising Folding user cache with DB data");
            FoldingUserCache.get().addAll(dbManager.getAllFoldingUsers());
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising Folding user cache", e.getCause());
        }

        try {
            LOGGER.debug("Initialising Folding team cache with DB data");
            FoldingTeamCache.get().addAll(dbManager.getAllFoldingTeams());
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising Folding team cache", e.getCause());
        }

        LOGGER.info("Caches initialised");
    }

    private void addHardware() {
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
                dbManager.createHardware(hardware);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial hardware data", e.getCause());
            }
        }

        LOGGER.debug("Initial hardware added to DB");
    }

    private void addFoldingUsers() {
        final List<FoldingUser> foldingUsers = List.of(
                FoldingUser.createWithoutId(
                        "BWG",
                        "BWG",
                        "8d10fbfda0813aa7288613e400484214",
                        "nvidia_gpu",
                        1,
                        239902
                ),
                FoldingUser.createWithoutId(
                        "Bastiaan_NL",
                        "Bastiaan_NL",
                        "d1ed404fdb11570aa07d2294601ad292",
                        "amd_gpu",
                        2,
                        239902
                ),
                FoldingUser.createWithoutId(
                        "BWG",
                        "BWG_With_Multiplier",
                        "8d10fbfda0813aa7288613e400484214",
                        "nvidia_gpu",
                        3,
                        239902
                ),
                FoldingUser.createWithoutId(
                        "Bastiaan_NL",
                        "Bastiaan_NL_With_Multiplier",
                        "d1ed404fdb11570aa07d2294601ad292",
                        "amd_gpu",
                        4,
                        239902
                )
        );

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                dbManager.createFoldingUser(foldingUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial Folding user data", e.getCause());
            }
        }

        LOGGER.debug("Initial Folding users added to DB");
    }

    private void addFoldingTeams() {
        final List<FoldingTeam> foldingTeams = List.of(
                new FoldingTeam.Builder("Freshly Waxed")
                        .teamDescription("BWG team")
                        .captainUserId(1)
                        .createTeam(),
                new FoldingTeam.Builder("Furry Folders")
                        .teamDescription("Bastiaan_NL team")
                        .captainUserId(2)
                        .createTeam(),
                new FoldingTeam.Builder("Test")
                        .teamDescription("Test team to try out multipliers")
                        .captainUserId(3) // BWG_With_Multiplier
                        .userId(4) // Bastiaan_NL_With_Multiplier
                        .createTeam()
        );

        for (final FoldingTeam foldingTeam : foldingTeams) {
            try {
                dbManager.createFoldingTeam(foldingTeam);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial Folding team data", e.getCause());
            }
        }

        LOGGER.debug("Initial Folding teams added to DB");
    }
}
