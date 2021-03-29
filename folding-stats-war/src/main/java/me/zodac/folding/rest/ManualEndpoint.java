package me.zodac.folding.rest;

import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.HardwareCategory;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.cache.FoldingUsersCache;
import me.zodac.folding.db.postgres.PostgresDbManager;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;


// TODO: [zodac] Manual endpoint to trigger actions
//   Replace with startup beans/Quartz scheduler
@Path("/manual/")
@RequestScoped
public class ManualEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManualEndpoint.class);

    @Context
    private UriInfo uriContext;

    @GET
    @Path("/load_db/")
    public Response loadDataIntoDb() {
        LOGGER.info("GET request received to load DB with initial data at '{}'", this.uriContext.getAbsolutePath());

        addHardwareCategories();
        addFoldingUsers();
        initCaches();

        return Response
                .ok()
                .build();
    }

    @GET
    @Path("/start_stats/")
    public Response startStatsParsing() {
        LOGGER.info("GET request received to start parsing Folding stats at '{}'", this.uriContext.getAbsolutePath());

        final FoldingUsersCache foldingUsersCache = FoldingUsersCache.getInstance();
        FoldingStatsParser.parseStats(foldingUsersCache.getAllUsers());

        return Response
                .ok()
                .build();
    }

    private static void initCaches() {
        final FoldingUsersCache foldingUsersCache = FoldingUsersCache.getInstance();

        try {
            for (final FoldingUser foldingUser : PostgresDbManager.getAllFoldingUsers()) {
                foldingUsersCache.addToCache(foldingUser);
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising Folding user cache", e.getCause());
        }
    }

    private static void addHardwareCategories() {
        final List<HardwareCategory> hardwareCategories = List.of(
                HardwareCategory.createWithoutId(
                        "Standard CPU", // ID 1
                        1.0D
                ),
                HardwareCategory.createWithoutId(
                        "Standard GPU", // ID 2
                        1.0D
                ),
                HardwareCategory.createWithoutId(
                        "Amazing CPU", // ID 3
                        0.8D
                ),
                HardwareCategory.createWithoutId(
                        "Amazing GPU", // ID 4
                        0.75D
                ),
                HardwareCategory.createWithoutId(
                        "Horrible CPU", // ID 5
                        1.2D
                ),
                HardwareCategory.createWithoutId(
                        "Horrible GPU", // ID 6
                        1.1D
                )
        );

        for (final HardwareCategory hardwareCategory : hardwareCategories) {
            try {
                PostgresDbManager.createHardwareCategory(hardwareCategory);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial hardware category data", e.getCause());
            }
        }
    }

    private static void addFoldingUsers() {
        final List<FoldingUser> foldingUsers = List.of(
                FoldingUser.createWithoutId(
                        "zodac",
                        "fc7d6837269d86784d8bfd0b386d6bca", // 39.5M
                        1,
                        "AMD X6"
                ),
                FoldingUser.createWithoutId(
                        "zodac",
                        "fe4ad3d7c2360a8cda89eaeab2b541f2", // TODO: [zodac] Confirm invalid response, 502 returned
                        5,
                        "Intel Pentium D"
                )
        );

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                PostgresDbManager.createFoldingUser(foldingUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Error loading initial Folding user data", e.getCause());
            }
        }
    }
}
