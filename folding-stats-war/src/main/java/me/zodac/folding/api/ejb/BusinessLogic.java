package me.zodac.folding.api.ejb;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;

/**
 * In order to decouple the REST layer from any business requirements, we move that logic into this interface, to be
 * implemented as an EJB. This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
public interface BusinessLogic {

    /**
     * Creates a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     */
    Hardware createHardware(final Hardware hardware);

    /**
     * Retrieves a {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     */
    Optional<Hardware> getHardware(final int hardwareId);

    /**
     * Retrieves all {@link Hardware}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     */
    Collection<Hardware> getAllHardware();

    /**
     * Deletes a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to delete
     */
    void deleteHardware(final Hardware hardware);

    /**
     * Creates a {@link Team}.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     */
    Team createTeam(final Team team);

    /**
     * Retrieves a {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     */
    Optional<Team> getTeam(final int teamId);

    /**
     * Retrieves all {@link Team}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     */
    Collection<Team> getAllTeams();

    /**
     * Updates an existing {@link Team}.
     *
     * @param updatedTeam the {@link Team} with updated values
     * @return the updated {@link Team}
     */
    Team updateTeam(final Team updatedTeam);

    /**
     * Deletes a {@link Team}.
     *
     * @param team the {@link Team} to delete
     */
    void deleteTeam(final Team team);

    /**
     * Retrieves a {@link User}, with the passkey unmodified.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUserWithPasskey(final int userId);

    /**
     * Retrieves a {@link User}, with the passkey masked.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUserWithoutPasskey(final int userId);

    /**
     * Retrieves all {@link User}, with the passkey unmodified.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithPasskeys();

    /**
     * Retrieves all {@link User}, with the passkey masked.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithoutPasskeys();

    /**
     * Retrieves a {@link Hardware} with the given name.
     *
     * <p>
     * Checks {@link #getAllHardware()} for a case-insensitive match on the {@link Hardware} name.
     *
     * <p>
     * If there is more than one result (though there should not be!), there is no guarantee on which will be
     * returned.
     *
     * @param hardwareName the name of the {@link Hardware}
     * @return an {@link Optional} of the matching {@link Hardware}
     */
    Optional<Hardware> getHardwareWithName(final String hardwareName);

    /**
     * Retrieves a {@link Team} with the given name.
     *
     * <p>
     * Checks {@link #getAllTeams()} for a case-insensitive match on the {@link Team} name.
     *
     * <p>
     * If there is more than one result (though there should not be!), there is no guarantee on which will be
     * returned.
     *
     * @param teamName the name of the {@link Team}
     * @return an {@link Optional} of the matching {@link Team}
     */
    Optional<Team> getTeamWithName(final String teamName);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Hardware}
     */
    Collection<User> getUsersWithHardware(final Hardware hardware);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeam(final Team team);

    /**
     * Retrieves a {@link User} with the matching {@code foldingUserName} and {@code passkey}. Since these are unique fields for a
     * {@link User} there should only be a single {@link User} returned (or none).
     *
     * @param foldingUserName the Folding@Home username of the {@link User} to be found
     * @param passkey         the passkey of the {@link User} to be found
     * @return an {@link Optional} of the matching {@link User}
     */
    Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey);

    /**
     * Creates a monthly result for the <code>Team Competition</code>.
     *
     * @param monthlyResult the result for a month of the <code>Team Competition</code>
     * @param utcTimestamp  the {@link java.time.ZoneOffset#UTC} timestamp for the result
     */
    void createMonthlyResult(final String monthlyResult, final LocalDateTime utcTimestamp);

    /**
     * Retrieves the result of the <code>Team Competition</code> for the provided {@link Month} and {@link Year}.
     *
     * @param month the {@link Month} of the result to be retrieved
     * @param year  the {@link Year} of the result to be retrieved
     * @return an {@link Optional} of the <code>Team Competition</code> result
     */
    Optional<String> getMonthlyResult(final Month month, final Year year);

    /**
     * Creates a {@link RetiredUserTcStats} for a {@link User} that has been deleted from a {@link Team}.
     *
     * @param team        the {@link Team} that the {@link User} has been deleted from
     * @param user        the {@link User} who is being deleted
     * @param userTcStats the {@link UserTcStats} at the time of deletion
     * @return the {@link RetiredUserTcStats}
     */
    RetiredUserTcStats createRetiredUser(final Team team, final User user, final UserTcStats userTcStats);

    /**
     * Retrieves all {@link RetiredUserTcStats} for the provided {@link Team}.
     *
     * @param team the {@link Team} whose retired users are to be found
     * @return a {@link Collection} of the retrieved {@link RetiredUserTcStats}
     */
    Collection<RetiredUserTcStats> getAllRetiredUsersForTeam(final Team team);

    /**
     * Authenticates a system user and retrieves its roles.
     *
     * <p>
     * The following scenarios are considered:
     * <ul>
     *     <li>The user does not exist</li>
     *     <li>The user exists but the password is incorrect</li>
     *     <li>The user exists, and the password is correct</li>
     * </ul>
     *
     * @param userName the system user username
     * @param password the system user password
     * @return the {@link UserAuthenticationResult}
     */
    UserAuthenticationResult authenticateSystemUser(final String userName, final String password);

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@code day}.
     *
     * @param user  the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year  the {@link Year} of the {@link HistoricStats}
     * @param month the {@link Month} of the {@link HistoricStats}
     * @param day   the day of the {@link Month} of the {@link HistoricStats}
     * @return the hourly {@link HistoricStats} for the {@link User}
     */
    Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month, final int day);

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@link Month}.
     *
     * @param user  the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year  the {@link Year} of the {@link HistoricStats}
     * @param month the {@link Month} of the {@link HistoricStats}
     * @return the daily {@link HistoricStats} for the {@link User}
     */
    Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month);

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@link Year}.
     *
     * @param user the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year the {@link Year} of the {@link HistoricStats}
     * @return the monthly {@link HistoricStats} for the {@link User}
     */
    Collection<HistoricStats> getHistoricStats(final User user, final Year year);

    /**
     * Creates a {@link UserStats} for the total overall stats for the provided {@link User}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}, or {@link UserStats#empty()}
     */
    UserStats createTotalStats(final UserStats userStats);

    /**
     * Retrieves the total {@link UserStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}, or {@link UserStats#empty()} if none can be found
     */
    UserStats getTotalStats(final User user);

    /**
     * Creates an {@link OffsetTcStats} defining the offset points/units for the provided {@link User}.
     *
     * <p>
     * If an {@link OffsetTcStats} already exists for the {@link User}, the existing values are updated to be the addition of both
     * {@link OffsetTcStats}.
     *
     * @param user          the {@link User} for whom the {@link OffsetTcStats} are being created
     * @param offsetTcStats the {@link OffsetTcStats} to be created
     * @return the created/updated {@link OffsetTcStats}, or {@link OffsetTcStats#empty()}
     */
    OffsetTcStats createOrUpdateOffsetStats(final User user, final OffsetTcStats offsetTcStats);

    /**
     * Retrieves the {@link OffsetTcStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link OffsetTcStats} are to be retrieved
     * @return the {@link OffsetTcStats} for the {@link User}, or {@link OffsetTcStats#empty()} if none can be found
     */
    OffsetTcStats getOffsetStats(final User user);

    /**
     * Deletes the {@link OffsetTcStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link OffsetTcStats} are to be deleted
     */
    void deleteOffsetStats(final User user);

    /**
     * Creates a {@link UserTcStats} for a {@link User}'s <code>Team Competition</code> stats for a specific hour.
     *
     * @param userTcStats the {@link UserTcStats} to be created
     * @return the created {@link UserTcStats}, or {@link UserTcStats#empty(int)}
     */
    UserTcStats createHourlyTcStats(final UserTcStats userTcStats);

    /**
     * Retrieves the latest {@link UserTcStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return the {@link UserTcStats} for the {@link User}, or {@link UserTcStats#empty(int)} if none can be found
     */
    UserTcStats getHourlyTcStats(final User user);

    /**
     * Checks if any {@link UserTcStats} for the <code>Team Competition</code> exist in the system.
     *
     * @return <code>true</code> if any {@link UserTcStats} have been created
     */
    boolean isAnyHourlyTcStatsExist();

    /**
     * Creates a {@link UserStats} for the initial overall stats for the provided {@link User} at the start of the monitoring period.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}, or {@link UserStats#empty()}
     */
    UserStats createInitialStats(final UserStats userStats);

    /**
     * Retrieves the initial {@link UserStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}, or {@link UserStats#empty()} if none can be found
     */
    UserStats getInitialStats(final User user);

    /**
     * Resets all {@link User}s for the <code>Team Competition</code>. Performs the following actions:
     *
     * <ul>
     *      <li>
     *          Zeroes out each {@link User}'s {@link UserTcStats} by setting their initial {@link UserStats} to their current total {@link UserStats}
     *          <b>NOTE:</b> This does not retrieve the latest online {@link UserStats} for the {@link User}, it simply retrieves the latest total
     *          {@link UserStats} available on the system from {@link #getTotalStats(User)}.
     *      </li>
     *      <li>
     *          Deletes any {@link OffsetTcStats} for the {@link User}s.
     *      </li>
     *      <li>
     *          Deletes any {@link RetiredUserTcStats}.
     *      </li>
     *      <li>
     *          Resets the {@link UserStats} and {@link UserTcStats} caches (if any are used).
     *      </li>
     * </ul>
     */
    void resetAllTeamCompetitionUserStats();
}
