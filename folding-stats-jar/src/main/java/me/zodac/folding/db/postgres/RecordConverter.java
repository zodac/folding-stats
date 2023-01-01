/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.db.postgres;

import static me.zodac.folding.db.postgres.gen.tables.Hardware.HARDWARE;
import static me.zodac.folding.db.postgres.gen.tables.SystemUsers.SYSTEM_USERS;
import static me.zodac.folding.db.postgres.gen.tables.Teams.TEAMS;
import static me.zodac.folding.db.postgres.gen.tables.Users.USERS;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import java.util.Set;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.util.DateTimeConverterUtils;
import me.zodac.folding.db.postgres.gen.tables.records.HardwareRecord;
import me.zodac.folding.db.postgres.gen.tables.records.MonthlyResultsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.RetiredUserStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.TeamsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserChangesRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserInitialStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserOffsetTcStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTcStatsHourlyRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTotalStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UsersRecord;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.api.util.LocalDateTimeGsonTypeAdapter;
import org.jooq.Record;

/**
 * Utility class that converts a {@link Record} into another POJO for {@link PostgresDbManager}.
 */
final class RecordConverter {

    // We don't try and reuse the GSON instance available in RestUtilConstants
    // This is because we do not want pretty-print enabled since that would increase the size of the JSON string being persisted in the DB
    static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, LocalDateTimeGsonTypeAdapter.getInstance())
        .disableHtmlEscaping()
        .create();

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
            HardwareMake.get(hardwareRecord.getHardwareMake()),
            HardwareType.get(hardwareRecord.getHardwareType()),
            hardwareRecord.getMultiplier().doubleValue(),
            hardwareRecord.getAveragePpd().longValue()
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
            userRecord.getIsCaptain() ? Role.CAPTAIN : Role.MEMBER
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
            DateTimeConverterUtils.toTimestamp(userTcStatsHourlyRecord.getUtcTimestamp()),
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
            DateTimeConverterUtils.toTimestamp(userTotalStatsRecord.getUtcTimestamp()),
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
            DateTimeConverterUtils.toTimestamp(userInitialStatsRecord.getUtcTimestamp()),
            userInitialStatsRecord.getInitialPoints(),
            userInitialStatsRecord.getInitialUnits()
        );
    }

    /**
     * Convert a {@link UserOffsetTcStatsRecord} into a {@link OffsetTcStats}.
     *
     * @param userOffsetTcStatsRecord the {@link UserOffsetTcStatsRecord} to convert
     * @return the converted {@link OffsetTcStats}
     */
    static OffsetTcStats toOffsetStats(final UserOffsetTcStatsRecord userOffsetTcStatsRecord) {
        return OffsetTcStats.create(
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
                DateTimeConverterUtils.toTimestamp(retiredUserStatsRecord.getUtcTimestamp()),
                retiredUserStatsRecord.getFinalPoints(),
                retiredUserStatsRecord.getFinalMultipliedPoints(),
                retiredUserStatsRecord.getFinalUnits()
            )
        );
    }

    /**
     * Convert a {@link MonthlyResultsRecord} into a {@link MonthlyResult}.
     *
     * @param monthlyResultsRecord the {@link MonthlyResultsRecord} to convert
     * @return the converted {@link MonthlyResult}
     */
    static MonthlyResult toMonthlyResult(final MonthlyResultsRecord monthlyResultsRecord) {
        final MonthlyResult monthlyResult = GSON.fromJson(monthlyResultsRecord.getJsonResult(), MonthlyResult.class);
        return MonthlyResult.updateWithEmptyCategories(monthlyResult);
    }

    /**
     * Convert a {@link Record} into an appropriate {@link UserAuthenticationResult}.
     *
     * <p>
     * Expects a field called {@code is_password_match} as a {@link Boolean} value determining whether
     * a supplied password matches the hashed value in the DB.
     *
     * @param systemUsersRecord the {@link Record} to convert
     * @return {@link UserAuthenticationResult#userRoles()} for a valid password, else {@link UserAuthenticationResult#invalidPassword()}
     */
    static UserAuthenticationResult toSystemUserAuthentication(final Record systemUsersRecord) {
        final boolean isMatchingPassword = isMatchingPassword(systemUsersRecord);

        if (isMatchingPassword) {
            final Set<String> roles = Set.of(systemUsersRecord.into(SYSTEM_USERS).getRoles());
            return UserAuthenticationResult.success(roles);
        }

        return UserAuthenticationResult.invalidPassword();
    }

    /**
     * Convert a {@link UserChangesRecord} into a {@link UserChange}.
     *
     * @param userChangesRecord the {@link UserChangesRecord} to convert
     * @return the converted {@link UserChange}
     */
    static UserChange toUserChange(final UserChangesRecord userChangesRecord) {
        return UserChange.create(
            userChangesRecord.getUserChangeId(),
            userChangesRecord.getCreatedUtcTimestamp(),
            userChangesRecord.getUpdatedUtcTimestamp(),
            GSON.fromJson(userChangesRecord.getPreviousUser(), User.class),
            GSON.fromJson(userChangesRecord.getNewUser(), User.class),
            UserChangeState.get(userChangesRecord.getState())
        );
    }

    private static boolean isMatchingPassword(final Record systemUsersRecord) {
        final String passwordMatchFieldName = "is_password_match";
        return systemUsersRecord.get(passwordMatchFieldName) != null
            && systemUsersRecord.get(passwordMatchFieldName, boolean.class);
    }
}
