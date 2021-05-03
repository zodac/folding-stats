package me.zodac.folding;


import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.exception.TeamNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserStatsOffset;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.TimeUtils;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.InitialStatsCache;
import me.zodac.folding.cache.OffsetStatsCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * In order to decouple the REST layer from the storage/persistence, we use this {@link StorageFacade} instead.
 * <p>
 * This way the {@link StorageFacade} is aware of caches or any other internal implementation, while the REST layer
 * does not need to know about them or any DBs being used.
 */
// TODO: [zodac] Should replace the cache miss warnings with some metrics instead?
// TODO: [zodac] Split into one Facade for POJOs and one for stats?
// TODO: [zodac] I really don't like how much logic is in here now, originally I planned for this just to avoid needing to specify
//  both DB and cache in the REST/EJB layer. I think it's gotten too big and needs to be scaled back...
@Singleton
public class StorageFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFacade.class);

    private final DbManager dbManager = DbManagerRetriever.get();
    private final TeamCache teamCache = TeamCache.get();
    private final UserCache userCache = UserCache.get();
    private final HardwareCache hardwareCache = HardwareCache.get();

    private final InitialStatsCache initialStatsCache = InitialStatsCache.get();
    private final OffsetStatsCache offsetStatsCache = OffsetStatsCache.get();
    private final RetiredTcStatsCache retiredStatsCache = RetiredTcStatsCache.get();
    private final TcStatsCache tcStatsCache = TcStatsCache.get();
    private final TotalStatsCache totalStatsCache = TotalStatsCache.get();

    public Hardware createHardware(final Hardware hardware) throws FoldingException, FoldingConflictException {
        // The REST input may not use the correct format for the ENUM, so we normalise it here
        hardware.setOperatingSystem(OperatingSystem.get(hardware.getOperatingSystem()).displayName());
        final Hardware hardwareWithId = dbManager.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    public Hardware getHardware(final int hardwareId) throws FoldingException, HardwareNotFoundException {
        try {
            return hardwareCache.get(hardwareId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find hardware with ID {} in cache", hardwareId, e);
        }

        LOGGER.trace("Cache miss! Get hardware");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Hardware hardwareFromDb = dbManager.getHardware(hardwareId);
        hardwareCache.add(hardwareFromDb);
        return hardwareFromDb;
    }

    public List<Hardware> getAllHardware() throws FoldingException {
        final List<Hardware> allHardware = hardwareCache.getAll();

        if (!allHardware.isEmpty()) {
            return allHardware;
        }

        LOGGER.trace("Cache miss! Get all hardware");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Hardware> allHardwareFromDb = dbManager.getAllHardware();
        hardwareCache.addAll(allHardwareFromDb);
        return allHardwareFromDb;
    }

    public void updateHardware(final Hardware hardware) throws FoldingException, HardwareNotFoundException, FoldingConflictException {
        dbManager.updateHardware(hardware);
        hardwareCache.add(hardware);
    }

    public void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException {
        dbManager.deleteHardware(hardwareId);
        hardwareCache.remove(hardwareId);
    }

    public User createUser(final User user) throws FoldingException, FoldingConflictException, FoldingExternalServiceException {
        // The REST input may not use the correct format for the ENUM, so we normalise it here
        user.setCategory(Category.get(user.getCategory()).displayName());
        final User userWithId = dbManager.createUser(user);
        userCache.add(userWithId);

        // TODO: [zodac] Should the StorageFacade be responsible for making this stats call? Or should it be the caller requesting it?
        // When adding a new user, we configure the initial stats DB/cache
        persistInitialUserStats(userWithId);
        // When adding a new user, we give an empty offset to the offset cache
        offsetStatsCache.add(userWithId.getId(), UserStatsOffset.empty());

        return userWithId;
    }

    public User getUser(final int userId) throws FoldingException, UserNotFoundException {
        return getUserWithPasskey(userId, true);
    }

    public User getUserWithPasskey(final int userId, final boolean showFullPasskeys) throws FoldingException, UserNotFoundException {
        try {
            final User user = userCache.get(userId);
            return showFullPasskeys ? user : User.hidePasskey(user);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find user with ID {} in cache", userId, e);
        }

        LOGGER.trace("Cache miss! Get user");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final User userFromDb = dbManager.getUser(userId);
        userCache.add(userFromDb);

        return showFullPasskeys ? userFromDb : User.hidePasskey(userFromDb);
    }

    public List<User> getAllUsers() throws FoldingException {
        return getAllUsersWithPasskeys(true);
    }

    public List<User> getAllUsersWithPasskeys(final boolean showFullPasskeys) throws FoldingException {
        final List<User> allUsers = userCache.getAll();

        if (!allUsers.isEmpty()) {
            if (showFullPasskeys) {
                return allUsers;
            }

            return allUsers.stream()
                    .map(User::hidePasskey)
                    .collect(toList());
        }

        LOGGER.trace("Cache miss! Get all users");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<User> allUsersFromDb = dbManager.getAllUsers();
        userCache.addAll(allUsersFromDb);

        if (showFullPasskeys) {
            return allUsersFromDb;
        }

        return allUsersFromDb.stream()
                .map(User::hidePasskey)
                .collect(toList());
    }

    public void updateUser(final User updatedUser) throws FoldingException, UserNotFoundException, FoldingConflictException, FoldingExternalServiceException {
        final User existingUser = getUser(updatedUser.getId());
        dbManager.updateUser(updatedUser);
        userCache.add(updatedUser);


        if (!existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())) {
            LOGGER.debug("User had state change to Folding username {} -> {}, recalculating initial stats", existingUser.getFoldingUserName(), updatedUser.getFoldingUserName());
            handleStateChangeForUserUpdate(updatedUser);
        } else if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey())) {
            LOGGER.debug("User had state change to passkey {} -> {}, recalculating initial stats", existingUser.getPasskey(), updatedUser.getPasskey());
            handleStateChangeForUserUpdate(updatedUser);
        } else if (existingUser.getHardwareId() != updatedUser.getHardwareId()) {
            LOGGER.debug("User had state change to hardware ID {} -> {}, recalculating initial stats", existingUser.getHardwareId(), updatedUser.getHardwareId());
            handleStateChangeForUserUpdate(updatedUser);
        }
    }

    // If a user is updated and their Folding username, hardware ID or passkey is changed, we need to update their initial offset again
    // The value should be: (new user info points - current TC points)
    private void handleStateChangeForUserUpdate(final User updatedUser) throws FoldingException, FoldingExternalServiceException {
        final UserStats updatedUserStats = FoldingStatsParser.getStatsForUser(updatedUser);
        final UserTcStats currentUserTcStats = getCurrentTcStatsForUserOrDefault(updatedUser);

        final UserStats newUserInitialStats = UserStats.create(updatedUser.getId(), updatedUserStats.getTimestamp(),
                Stats.create(updatedUserStats.getPoints() - currentUserTcStats.getMultipliedPoints(), updatedUserStats.getUnits() - currentUserTcStats.getUnits())
        );

        dbManager.persistInitialUserStats(newUserInitialStats);
        initialStatsCache.add(updatedUser.getId(), newUserInitialStats.getStats());
    }

    private UserTcStats getCurrentTcStatsForUserOrDefault(final User updatedUser) throws FoldingException {
        try {
            return getTcStatsForUser(updatedUser.getId());
        } catch (final UserNotFoundException e) {
            LOGGER.debug("Unable to find user with ID: {}, using 0 values", e.getId(), e);
            return UserTcStats.empty(updatedUser.getId());
        }
    }

    public void deleteUser(final int userId) throws FoldingException, FoldingConflictException {
        dbManager.deleteUser(userId);
        userCache.remove(userId);
    }

    public Team createTeam(final Team team) throws FoldingException, FoldingConflictException {
        final Team teamWithId = dbManager.createTeam(team);
        teamCache.add(teamWithId);
        return teamWithId;
    }

    public Team getTeam(final int teamId) throws FoldingException, TeamNotFoundException {
        try {
            return teamCache.get(teamId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find team with ID {} in cache", teamId, e);
        }

        LOGGER.trace("Cache miss! Get team");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Team teamFromDb = dbManager.getTeam(teamId);
        teamCache.add(teamFromDb);
        return teamFromDb;
    }

    public List<Team> getAllTeams() throws FoldingException {
        final List<Team> allTeams = teamCache.getAll();

        if (!allTeams.isEmpty()) {
            return allTeams;
        }

        LOGGER.trace("Cache miss! Get all teams");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Team> allTeamsFromDb = dbManager.getAllTeams();
        teamCache.addAll(allTeamsFromDb);
        return allTeamsFromDb;
    }

    public void updateTeam(final Team team) throws FoldingException, TeamNotFoundException, FoldingConflictException {
        dbManager.updateTeam(team);
        teamCache.add(team);
    }

    public void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException {
        dbManager.deleteTeam(teamId);
        teamCache.remove(teamId);
    }

    public RetiredUserTcStats getRetiredUser(final int retiredUserId) throws FoldingException {
        final Optional<RetiredUserTcStats> optionalRetiredUserStats = retiredStatsCache.get(retiredUserId);

        if (optionalRetiredUserStats.isPresent()) {
            return optionalRetiredUserStats.get();
        }

        LOGGER.trace("Cache miss! Retired user TC stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final RetiredUserTcStats retiredUserTcStatsFromDb = dbManager.getRetiredUserStats(retiredUserId);
        retiredStatsCache.add(retiredUserTcStatsFromDb);
        return retiredUserTcStatsFromDb;
    }

    public Team retireUser(final int teamId, final int userId) throws FoldingConflictException, UserNotFoundException, FoldingException, TeamNotFoundException {
        final User retiredUser = User.retireUser(getUser(userId));
        dbManager.updateUser(retiredUser);
        userCache.add(retiredUser);

        final UserTcStats userStats = getTcStatsForUser(retiredUser.getId());
        final int retiredUserId = dbManager.persistRetiredUserStats(teamId, retiredUser.getDisplayName(), userStats);
        retiredStatsCache.add(RetiredUserTcStats.create(retiredUserId, teamId, retiredUser.getDisplayName(), userStats));

        final Team team = getTeam(teamId);
        final Team updatedTeam = Team.retireUser(team, userStats.getUserId(), retiredUserId);
        dbManager.updateTeam(updatedTeam);
        teamCache.add(updatedTeam);
        return updatedTeam;
    }

    public Team unretireUser(final int teamId, final int retiredUserId) throws UserNotFoundException, FoldingException, FoldingConflictException, TeamNotFoundException, FoldingExternalServiceException {
        // Get the original user ID
        final RetiredUserTcStats retiredUserStats = dbManager.getRetiredUserStats(retiredUserId); // TODO: [zodac] Add another cache? I like caches :)
        final int userId = retiredUserStats.getUserId();

        // Update the user to no longer be retired
        final User user = getUser(userId);
        final User unretiredUser = User.unretireUser(user);
        dbManager.updateUser(unretiredUser);
        userCache.add(unretiredUser);

        // Update the user's initial stats to their current total
        persistInitialUserStats(unretiredUser);

        final Team team = getTeam(teamId);

        // If retired user is from this team, AND still listed as retired (meaning the monthly reset has not removed this user from the team),
        // we want to allow them to keep their points pre-retirement
        // We add an offset for the user based on their retired stats
        if (teamId == retiredUserStats.getTeamId() && team.getRetiredUserIds().contains(retiredUserId)) {
            LOGGER.debug("Un-retiring user for original team, adding offset: {}", retiredUserStats);
            addOffsetStats(unretiredUser.getId(), UserStatsOffset.create(retiredUserStats.getMultipliedPoints(), retiredUserStats.getUnits()));
        } else {
            LOGGER.debug("User {} was not previously a member {} of this team {}, resetting offset", retiredUserStats, team.getRetiredUserIds(), teamId);
            addOffsetStats(unretiredUser.getId(), UserStatsOffset.empty());
        }

        // Update the team with the new user
        // Team may not be the original team the user retired from, so may not exist in this team
        // But we do not remove the retired user from the original team, unless it is the same team
        final Team updatedTeam = Team.unretireUser(team, userId, retiredUserId);
        dbManager.updateTeam(updatedTeam);
        teamCache.add(updatedTeam);
        return updatedTeam;
    }

    public void persistInitialUserStats(final User user) throws FoldingException, FoldingExternalServiceException {
        final UserStats currentUserStats = FoldingStatsParser.getStatsForUser(user);
        persistInitialUserStats(currentUserStats);
    }

    public void persistInitialUserStats(final UserStats userStats) throws FoldingException {
        dbManager.persistInitialUserStats(userStats);
        initialStatsCache.add(userStats.getUserId(), userStats.getStats());
    }

    public Stats getInitialStatsForUser(final int userId) throws UserNotFoundException, FoldingException {
        final Stats initialUserStats = dbManager.getInitialUserStats(userId);
        initialStatsCache.add(userId, initialUserStats);
        return initialUserStats;
    }

    public Map<Integer, Stats> getInitialStatsForUsers(final List<Integer> userIds) throws FoldingException {
        final Map<Integer, Stats> cachedInitialStats = new HashMap<>(userIds.size());
        for (final int userId : userIds) {
            final Optional<Stats> optionalStats = initialStatsCache.get(userId);
            optionalStats.ifPresent(stats -> cachedInitialStats.put(userId, stats));
        }

        if (cachedInitialStats.size() == userIds.size()) {
            return cachedInitialStats;
        }

        LOGGER.debug("Found {} cached initial stats for {} user IDs, checking DB instead", cachedInitialStats.size(), userIds.size());
        return dbManager.getInitialUserStats(userIds);
    }

    public Map<Integer, User> getActiveTcUsers(final List<Team> teams) {
        return teams
                .stream()
                .map(Team::getUserIds)
                .flatMap(Collection::stream)
                .map(userId -> UserCache.get().getOrNull(userId))
                .filter(user -> Objects.nonNull(user) && !user.isRetired())
                .collect(toMap(User::getId, user -> user));
    }

    public void persistHourlyTcUserStats(final List<UserTcStats> tcStatsForUsers) throws FoldingException {
        dbManager.persistHourlyTcUserStats(tcStatsForUsers);

        for (final UserTcStats userTcStats : tcStatsForUsers) {
            tcStatsCache.add(userTcStats.getUserId(), userTcStats);
        }
    }

    public UserTcStats getTcStatsForUser(final int userId) throws UserNotFoundException, FoldingException {
        final Optional<UserTcStats> optionalUserTcStats = tcStatsCache.get(userId);

        if (optionalUserTcStats.isPresent()) {
            return optionalUserTcStats.get();
        }

        LOGGER.trace("Cache miss! Current TC stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final UserTcStats userTcStatsFromDb = dbManager.getCurrentTcStats(userId);
        tcStatsCache.add(userId, userTcStatsFromDb);
        return userTcStatsFromDb;
    }

    public Map<LocalDate, UserTcStats> getDailyUserTcStats(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        return dbManager.getDailyUserTcStats(userId, month, year);
    }

    public void addOffsetStats(final int userId, final UserStatsOffset userStatsOffset) throws FoldingException {
        dbManager.addOffsetStats(userId, userStatsOffset);
        offsetStatsCache.add(userId, userStatsOffset);
    }

    public void addOrUpdateOffsetStats(final int userId, final UserStatsOffset userStatsOffset) throws FoldingException {
        final UserStatsOffset userStatsOffsetFromDb = dbManager.addOrUpdateOffsetStats(userId, userStatsOffset);
        offsetStatsCache.add(userId, userStatsOffsetFromDb);
    }

    public Map<Integer, UserStatsOffset> getOffsetStatsForUsers(final List<Integer> userIds) throws FoldingException {
        if (offsetStatsCache.isNotEmpty()) {
            final Map<Integer, UserStatsOffset> offsetStatsByUserId = new HashMap<>(userIds.size());

            for (final int userId : userIds) {
                final Optional<UserStatsOffset> optional = offsetStatsCache.get(userId);
                offsetStatsByUserId.put(userId, optional.orElse(UserStatsOffset.empty()));
            }

            if (offsetStatsByUserId.size() == userIds.size()) {
                return offsetStatsByUserId;
            }
        }

        LOGGER.trace("Cache miss! All user offset stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Map<Integer, UserStatsOffset> offsetStatsByUserIdFromDb = dbManager.getOffsetStats(userIds);
        offsetStatsCache.addAll(offsetStatsByUserIdFromDb);
        return offsetStatsByUserIdFromDb;
    }

    public void clearOffsetStats() throws FoldingConflictException, FoldingException {
        dbManager.clearOffsetStats();
        offsetStatsCache.clearOffsets();
    }

    public void persistTotalUserStats(final List<UserStats> stats) throws FoldingException {
        dbManager.persistTotalUserStats(stats);
        totalStatsCache.addAll(stats);
    }

    public Stats getTotalStatsForUser(final int userId) throws FoldingException {
        final Optional<Stats> optionalTotalStats = totalStatsCache.get(userId);

        if (optionalTotalStats.isPresent()) {
            return optionalTotalStats.get();
        }

        LOGGER.trace("Cache miss! Total stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Stats userTotalStatsFromDb = dbManager.getTotalStats(userId);
        totalStatsCache.add(userId, userTotalStatsFromDb);
        return userTotalStatsFromDb;
    }

    public void updateInitialStatsForUser(final User user) throws UserNotFoundException, FoldingException {
        LOGGER.info("Updating initial stats for user: {}", user);
        final Stats totalStats = getTotalStatsForUser(user.getId());
        persistInitialUserStats(UserStats.create(user.getId(), TimeUtils.getCurrentUtcTimestamp(), totalStats));
        initialStatsCache.add(user.getId(), totalStats);
    }
}
