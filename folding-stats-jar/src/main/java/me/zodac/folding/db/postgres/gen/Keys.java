/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen;


import me.zodac.folding.db.postgres.gen.tables.Hardware;
import me.zodac.folding.db.postgres.gen.tables.MonthlyResults;
import me.zodac.folding.db.postgres.gen.tables.RetiredUserStats;
import me.zodac.folding.db.postgres.gen.tables.SystemUsers;
import me.zodac.folding.db.postgres.gen.tables.Teams;
import me.zodac.folding.db.postgres.gen.tables.UserInitialStats;
import me.zodac.folding.db.postgres.gen.tables.UserOffsetTcStats;
import me.zodac.folding.db.postgres.gen.tables.UserTcStatsHourly;
import me.zodac.folding.db.postgres.gen.tables.UserTotalStats;
import me.zodac.folding.db.postgres.gen.tables.Users;
import me.zodac.folding.db.postgres.gen.tables.records.HardwareRecord;
import me.zodac.folding.db.postgres.gen.tables.records.MonthlyResultsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.RetiredUserStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.SystemUsersRecord;
import me.zodac.folding.db.postgres.gen.tables.records.TeamsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserInitialStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserOffsetTcStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTcStatsHourlyRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UserTotalStatsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UsersRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<HardwareRecord> HARDWARE_PKEY = Internal.createUniqueKey(Hardware.HARDWARE, DSL.name("hardware_pkey"), new TableField[] { Hardware.HARDWARE.HARDWARE_ID }, true);
    public static final UniqueKey<HardwareRecord> UNIQUE_HARDWARE = Internal.createUniqueKey(Hardware.HARDWARE, DSL.name("unique_hardware"), new TableField[] { Hardware.HARDWARE.HARDWARE_NAME, Hardware.HARDWARE.OPERATING_SYSTEM }, true);
    public static final UniqueKey<MonthlyResultsRecord> MONTHLY_RESULTS_PKEY = Internal.createUniqueKey(MonthlyResults.MONTHLY_RESULTS, DSL.name("monthly_results_pkey"), new TableField[] { MonthlyResults.MONTHLY_RESULTS.UTC_TIMESTAMP }, true);
    public static final UniqueKey<RetiredUserStatsRecord> RETIRED_USER_STATS_PKEY = Internal.createUniqueKey(RetiredUserStats.RETIRED_USER_STATS, DSL.name("retired_user_stats_pkey"), new TableField[] { RetiredUserStats.RETIRED_USER_STATS.RETIRED_USER_ID }, true);
    public static final UniqueKey<RetiredUserStatsRecord> RETIRED_USER_STATS_USER_ID_KEY = Internal.createUniqueKey(RetiredUserStats.RETIRED_USER_STATS, DSL.name("retired_user_stats_user_id_key"), new TableField[] { RetiredUserStats.RETIRED_USER_STATS.USER_ID }, true);
    public static final UniqueKey<SystemUsersRecord> SYSTEM_USERS_PKEY = Internal.createUniqueKey(SystemUsers.SYSTEM_USERS, DSL.name("system_users_pkey"), new TableField[] { SystemUsers.SYSTEM_USERS.USER_NAME }, true);
    public static final UniqueKey<TeamsRecord> TEAMS_PKEY = Internal.createUniqueKey(Teams.TEAMS, DSL.name("teams_pkey"), new TableField[] { Teams.TEAMS.TEAM_ID }, true);
    public static final UniqueKey<TeamsRecord> TEAMS_TEAM_NAME_KEY = Internal.createUniqueKey(Teams.TEAMS, DSL.name("teams_team_name_key"), new TableField[] { Teams.TEAMS.TEAM_NAME }, true);
    public static final UniqueKey<UserInitialStatsRecord> USER_INITIAL_STATS_PKEY = Internal.createUniqueKey(UserInitialStats.USER_INITIAL_STATS, DSL.name("user_initial_stats_pkey"), new TableField[] { UserInitialStats.USER_INITIAL_STATS.USER_ID, UserInitialStats.USER_INITIAL_STATS.UTC_TIMESTAMP }, true);
    public static final UniqueKey<UserOffsetTcStatsRecord> USER_OFFSET_TC_STATS_PKEY = Internal.createUniqueKey(UserOffsetTcStats.USER_OFFSET_TC_STATS, DSL.name("user_offset_tc_stats_pkey"), new TableField[] { UserOffsetTcStats.USER_OFFSET_TC_STATS.USER_ID, UserOffsetTcStats.USER_OFFSET_TC_STATS.UTC_TIMESTAMP }, true);
    public static final UniqueKey<UserOffsetTcStatsRecord> USER_OFFSET_TC_STATS_USER_ID_KEY = Internal.createUniqueKey(UserOffsetTcStats.USER_OFFSET_TC_STATS, DSL.name("user_offset_tc_stats_user_id_key"), new TableField[] { UserOffsetTcStats.USER_OFFSET_TC_STATS.USER_ID }, true);
    public static final UniqueKey<UserTcStatsHourlyRecord> USER_TC_STATS_HOURLY_PKEY = Internal.createUniqueKey(UserTcStatsHourly.USER_TC_STATS_HOURLY, DSL.name("user_tc_stats_hourly_pkey"), new TableField[] { UserTcStatsHourly.USER_TC_STATS_HOURLY.USER_ID, UserTcStatsHourly.USER_TC_STATS_HOURLY.UTC_TIMESTAMP }, true);
    public static final UniqueKey<UserTotalStatsRecord> USER_TOTAL_STATS_PKEY = Internal.createUniqueKey(UserTotalStats.USER_TOTAL_STATS, DSL.name("user_total_stats_pkey"), new TableField[] { UserTotalStats.USER_TOTAL_STATS.USER_ID, UserTotalStats.USER_TOTAL_STATS.UTC_TIMESTAMP }, true);
    public static final UniqueKey<UsersRecord> UNIQUE_USER = Internal.createUniqueKey(Users.USERS, DSL.name("unique_user"), new TableField[] { Users.USERS.FOLDING_USERNAME, Users.USERS.PASSKEY }, true);
    public static final UniqueKey<UsersRecord> USERS_PKEY = Internal.createUniqueKey(Users.USERS, DSL.name("users_pkey"), new TableField[] { Users.USERS.USER_ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<RetiredUserStatsRecord, TeamsRecord> RETIRED_USER_STATS__FK_TEAM_ID = Internal.createForeignKey(RetiredUserStats.RETIRED_USER_STATS, DSL.name("fk_team_id"), new TableField[] { RetiredUserStats.RETIRED_USER_STATS.TEAM_ID }, Keys.TEAMS_PKEY, new TableField[] { Teams.TEAMS.TEAM_ID }, true);
    public static final ForeignKey<UserInitialStatsRecord, UsersRecord> USER_INITIAL_STATS__FK_USER_ID = Internal.createForeignKey(UserInitialStats.USER_INITIAL_STATS, DSL.name("fk_user_id"), new TableField[] { UserInitialStats.USER_INITIAL_STATS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<UserOffsetTcStatsRecord, UsersRecord> USER_OFFSET_TC_STATS__FK_USER_ID = Internal.createForeignKey(UserOffsetTcStats.USER_OFFSET_TC_STATS, DSL.name("fk_user_id"), new TableField[] { UserOffsetTcStats.USER_OFFSET_TC_STATS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<UserTcStatsHourlyRecord, UsersRecord> USER_TC_STATS_HOURLY__FK_USER_ID = Internal.createForeignKey(UserTcStatsHourly.USER_TC_STATS_HOURLY, DSL.name("fk_user_id"), new TableField[] { UserTcStatsHourly.USER_TC_STATS_HOURLY.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<UserTotalStatsRecord, UsersRecord> USER_TOTAL_STATS__FK_USER_ID = Internal.createForeignKey(UserTotalStats.USER_TOTAL_STATS, DSL.name("fk_user_id"), new TableField[] { UserTotalStats.USER_TOTAL_STATS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<UsersRecord, HardwareRecord> USERS__FK_HARDWARE_ID = Internal.createForeignKey(Users.USERS, DSL.name("fk_hardware_id"), new TableField[] { Users.USERS.HARDWARE_ID }, Keys.HARDWARE_PKEY, new TableField[] { Hardware.HARDWARE.HARDWARE_ID }, true);
    public static final ForeignKey<UsersRecord, TeamsRecord> USERS__FK_TEAM_ID = Internal.createForeignKey(Users.USERS, DSL.name("fk_team_id"), new TableField[] { Users.USERS.TEAM_ID }, Keys.TEAMS_PKEY, new TableField[] { Teams.TEAMS.TEAM_ID }, true);
}
