package me.zodac.folding.db.postgres;

import static me.zodac.folding.db.postgres.gen.tables.Hardware.HARDWARE;
import static me.zodac.folding.db.postgres.gen.tables.SystemUsers.SYSTEM_USERS;
import static me.zodac.folding.db.postgres.gen.tables.Teams.TEAMS;
import static me.zodac.folding.db.postgres.gen.tables.Users.USERS;

import java.util.Set;
import me.zodac.folding.api.SystemUserAuthentication;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.db.postgres.gen.tables.records.HardwareRecord;
import me.zodac.folding.db.postgres.gen.tables.records.MonthlyResultsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.RetiredUserStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.TeamsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserInitialStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserOffsetTcStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTcStatsHourlyRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTotalStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UsersRecord;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.jooq.Record;

/**
 * Utility class that converts a {@link Record} into another POJO for {@link PostgresDbManager}.
 */
final class RecordConverter {

    private RecordConverter() {

    }

    /**
     * Convert a {@link HardwareRecord} into a {@link Hardware}.
     *
     * @param hardwareRecord the {@link HardwareRecord} to convert
     * @return the converted {@link Hardware}
     */
    static Hardware toHardware(final HardwareRecord hardwareRecord) {
        return Hardware.create(
            hardwareRecord.getHardwareId(),
            hardwareRecord.getHardwareName(),
            hardwareRecord.getDisplayName(),
            OperatingSystem.get(hardwareRecord.getOperatingSystem()),
            hardwareRecord.getMultiplier().doubleValue()
        );
    }

    /**
     * Convert a {@link TeamsRecord} into a {@link Team}.
     *
     * @param teamRecord the {@link TeamsRecord} to convert
     * @return the converted {@link Team}
     */
    static Team toTeam(final TeamsRecord teamRecord) {
        return Team.create(
            teamRecord.getTeamId(),
            teamRecord.getTeamName(),
            teamRecord.getTeamDescription(),
            teamRecord.getForumLink()
        );
    }

    /**
     * Convert a {@link Record} into a {@link User}. The {@link Record} should contain info for the {@link User} and also the {@link Hardware} and
     * {@link Team}.
     *
     * @param joinedRecord the {@link Record} to convert
     * @return the converted {@link Hardware}
     * @see #toHardware(HardwareRecord)
     * @see #toTeam(TeamsRecord)
     */
    static User toUser(final Record joinedRecord) {
        final UsersRecord userRecord = joinedRecord.into(USERS);
        return User.create(
            userRecord.getUserId(),
            userRecord.getFoldingUsername(),
            userRecord.getDisplayUsername(),
            userRecord.getPasskey(),
            Category.get(userRecord.getCategory()),
            userRecord.getProfileLink(),
            userRecord.getLiveStatsLink(),
            toHardware(joinedRecord.into(HARDWARE)),
            toTeam(joinedRecord.into(TEAMS)),
            userRecord.getIsCaptain()
        );
    }

    /**
     * Convert a {@link UserTcStatsHourlyRecord} into a {@link UserTcStats}.
     *
     * @param userTcStatsHourlyRecord the {@link UserTcStatsHourlyRecord} to convert
     * @return the converted {@link UserTcStats}
     */
    static UserTcStats toUserTcStats(final UserTcStatsHourlyRecord userTcStatsHourlyRecord) {
        return UserTcStats.create(
            userTcStatsHourlyRecord.getUserId(),
            DateTimeUtils.toTimestamp(userTcStatsHourlyRecord.getUtcTimestamp()),
            userTcStatsHourlyRecord.getTcPoints(),
            userTcStatsHourlyRecord.getTcPointsMultiplied(),
            userTcStatsHourlyRecord.getTcUnits()
        );
    }

    /**
     * Convert a {@link UserTcStatsHourlyRecord} into a {@link HistoricStats}.
     *
     * @param userTcStatsHourlyRecord the {@link UserTcStatsHourlyRecord} to convert
     * @return the converted {@link HistoricStats}
     */
    static HistoricStats toHistoricStats(final UserTcStatsHourlyRecord userTcStatsHourlyRecord) {
        return HistoricStats.create(
            userTcStatsHourlyRecord.getUtcTimestamp(),
            userTcStatsHourlyRecord.getTcPoints(),
            userTcStatsHourlyRecord.getTcPointsMultiplied(),
            userTcStatsHourlyRecord.getTcUnits()
        );
    }

    /**
     * Convert a {@link UserTotalStatsRecord} into a {@link UserStats}.
     *
     * @param userTotalStatsRecord the {@link UserTotalStatsRecord} to convert
     * @return the converted {@link UserStats}
     */
    static UserStats toUserStats(final UserTotalStatsRecord userTotalStatsRecord) {
        return UserStats.create(
            userTotalStatsRecord.getUserId(),
            DateTimeUtils.toTimestamp(userTotalStatsRecord.getUtcTimestamp()),
            userTotalStatsRecord.getTotalPoints(),
            userTotalStatsRecord.getTotalUnits()
        );
    }

    /**
     * Convert a {@link UserInitialStatsRecord} into a {@link UserStats}.
     *
     * @param userInitialStatsRecord the {@link UserInitialStatsRecord} to convert
     * @return the converted {@link UserStats}
     */
    static UserStats toUserStats(final UserInitialStatsRecord userInitialStatsRecord) {
        return UserStats.create(
            userInitialStatsRecord.getUserId(),
            DateTimeUtils.toTimestamp(userInitialStatsRecord.getUtcTimestamp()),
            userInitialStatsRecord.getInitialPoints(),
            userInitialStatsRecord.getInitialUnits()
        );
    }

    /**
     * Convert a {@link UserOffsetTcStatsRecord} into a {@link OffsetStats}.
     *
     * @param userOffsetTcStatsRecord the {@link UserOffsetTcStatsRecord} to convert
     * @return the converted {@link OffsetStats}
     */
    static OffsetStats toOffsetStats(final UserOffsetTcStatsRecord userOffsetTcStatsRecord) {
        return OffsetStats.create(
            userOffsetTcStatsRecord.getOffsetPoints(),
            userOffsetTcStatsRecord.getOffsetMultipliedPoints(),
            userOffsetTcStatsRecord.getOffsetUnits()
        );
    }

    /**
     * Convert a {@link RetiredUserStatsRecord} into a {@link RetiredUserTcStats}.
     *
     * @param retiredUserStatsRecord the {@link RetiredUserStatsRecord} to convert
     * @return the converted {@link RetiredUserTcStats}
     */
    static RetiredUserTcStats toRetiredUserStats(final RetiredUserStatsRecord retiredUserStatsRecord) {
        return RetiredUserTcStats.create(
            retiredUserStatsRecord.getRetiredUserId(),
            retiredUserStatsRecord.getTeamId(),
            retiredUserStatsRecord.getDisplayUsername(),
            UserTcStats.create(
                retiredUserStatsRecord.getUserId(),
                DateTimeUtils.toTimestamp(retiredUserStatsRecord.getUtcTimestamp()),
                retiredUserStatsRecord.getFinalPoints(),
                retiredUserStatsRecord.getFinalMultipliedPoints(),
                retiredUserStatsRecord.getFinalUnits()
            )
        );
    }

    /**
     * Convert a {@link MonthlyResultsRecord} into a {@link String} containing the monthly result.
     *
     * @param monthlyResultsRecord the {@link MonthlyResultsRecord} to convert
     * @return the converted {@link String}
     */
    static String toMonthlyResults(final MonthlyResultsRecord monthlyResultsRecord) {
        return monthlyResultsRecord.getJsonResult();
    }

    /**
     * Convert a {@link Record} into an appropriate {@link SystemUserAuthentication}.
     * Expects a field called <code>is_password_match</code> as a {@link Boolean} value determining whether
     * a supplied password matches a has in the DB.
     *
     * @param systemUsersRecord the {@link Record} to convert
     * @return {@link SystemUserAuthentication#getUserRoles()} if valid password was supplied, else {@link SystemUserAuthentication#invalidPassword()}
     */
    static SystemUserAuthentication toSystemUserAuthentication(final Record systemUsersRecord) {
        final boolean isPasswordMatch = getPasswordMatchValue(systemUsersRecord);

        if (isPasswordMatch) {
            final Set<String> roles = Set.of(systemUsersRecord.into(SYSTEM_USERS).getRoles());
            return SystemUserAuthentication.success(roles);
        }

        return SystemUserAuthentication.invalidPassword();
    }

    private static boolean getPasswordMatchValue(final Record systemUsersRecord) {
        final String passwordMatchFieldName = "is_password_match";
        return systemUsersRecord.get(passwordMatchFieldName) != null
            && systemUsersRecord.get(passwordMatchFieldName, boolean.class);
    }
}
