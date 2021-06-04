package me.zodac.folding.db.postgres;

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
import me.zodac.folding.db.postgres.gen.tables.records.RetiredUserStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.TeamsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserInitialStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserOffsetTcStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTcStatsHourlyRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTotalStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UsersRecord;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.jooq.Record;

import static me.zodac.folding.db.postgres.gen.tables.Hardware.HARDWARE;
import static me.zodac.folding.db.postgres.gen.tables.Teams.TEAMS;
import static me.zodac.folding.db.postgres.gen.tables.Users.USERS;

/**
 * Utility class that converts a {@link org.jooq.Record} into another POJO for {@link PostgresDbManager}.
 */
final class RecordConverter {

    private RecordConverter() {
        
    }

    static Hardware toHardware(final HardwareRecord hardwareRecord) {
        return Hardware.create(
                hardwareRecord.getHardwareId(),
                hardwareRecord.getHardwareName(),
                hardwareRecord.getDisplayName(),
                OperatingSystem.get(hardwareRecord.getOperatingSystem()),
                hardwareRecord.getMultiplier().doubleValue()
        );
    }

    static Team toTeam(final TeamsRecord teamRecord) {
        return Team.create(
                teamRecord.getTeamId(),
                teamRecord.getTeamName(),
                teamRecord.getTeamDescription(),
                teamRecord.getForumLink()
        );
    }

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

    static UserTcStats toUserTcStats(final UserTcStatsHourlyRecord userTcStatsHourlyRecord) {
        return UserTcStats.create(
                userTcStatsHourlyRecord.getUserId(),
                DateTimeUtils.toTimestamp(userTcStatsHourlyRecord.getUtcTimestamp()),
                userTcStatsHourlyRecord.getTcPoints(),
                userTcStatsHourlyRecord.getTcPointsMultiplied(),
                userTcStatsHourlyRecord.getTcUnits()
        );
    }

    static HistoricStats toHistoricStats(final UserTcStatsHourlyRecord userTcStatsHourlyRecord) {
        return HistoricStats.create(
                userTcStatsHourlyRecord.getUtcTimestamp(),
                userTcStatsHourlyRecord.getTcPoints(),
                userTcStatsHourlyRecord.getTcPointsMultiplied(),
                userTcStatsHourlyRecord.getTcUnits()
        );
    }

    static UserStats toUserStats(final UserTotalStatsRecord userTotalStatsRecord) {
        return UserStats.createWithPointsAndUnits(
                userTotalStatsRecord.getUserId(),
                DateTimeUtils.toTimestamp(userTotalStatsRecord.getUtcTimestamp()),
                userTotalStatsRecord.getTotalPoints(),
                userTotalStatsRecord.getTotalUnits()
        );
    }

    static UserStats toUserStats(final UserInitialStatsRecord userInitialStatsRecord) {
        return UserStats.createWithPointsAndUnits(
                userInitialStatsRecord.getUserId(),
                DateTimeUtils.toTimestamp(userInitialStatsRecord.getUtcTimestamp()),
                userInitialStatsRecord.getInitialPoints(),
                userInitialStatsRecord.getInitialUnits()
        );
    }

    static OffsetStats toOffsetStats(final UserOffsetTcStatsRecord userOffsetTcStatsRecord) {
        return OffsetStats.create(
                userOffsetTcStatsRecord.getOffsetPoints(),
                userOffsetTcStatsRecord.getOffsetMultipliedPoints(),
                userOffsetTcStatsRecord.getOffsetUnits()
        );
    }

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
}
