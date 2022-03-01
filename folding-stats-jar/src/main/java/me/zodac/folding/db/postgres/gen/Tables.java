/*
 * This file is generated by jOOQ.
 */

package me.zodac.folding.db.postgres.gen;


import me.zodac.folding.db.postgres.gen.tables.Hardware;
import me.zodac.folding.db.postgres.gen.tables.MonthlyResults;
import me.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders;
import me.zodac.folding.db.postgres.gen.tables.RetiredUserStats;
import me.zodac.folding.db.postgres.gen.tables.SystemUsers;
import me.zodac.folding.db.postgres.gen.tables.Teams;
import me.zodac.folding.db.postgres.gen.tables.UserChanges;
import me.zodac.folding.db.postgres.gen.tables.UserInitialStats;
import me.zodac.folding.db.postgres.gen.tables.UserOffsetTcStats;
import me.zodac.folding.db.postgres.gen.tables.UserTcStatsHourly;
import me.zodac.folding.db.postgres.gen.tables.UserTotalStats;
import me.zodac.folding.db.postgres.gen.tables.Users;
import me.zodac.folding.db.postgres.gen.tables.records.PgpArmorHeadersRecord;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;


/**
 * Convenience access to all tables in public.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Tables {

    /**
     * The table <code>public.hardware</code>.
     */
    public static final Hardware HARDWARE = Hardware.HARDWARE;

    /**
     * The table <code>public.monthly_results</code>.
     */
    public static final MonthlyResults MONTHLY_RESULTS = MonthlyResults.MONTHLY_RESULTS;

    /**
     * The table <code>public.pgp_armor_headers</code>.
     */
    public static final PgpArmorHeaders PGP_ARMOR_HEADERS = PgpArmorHeaders.PGP_ARMOR_HEADERS;

    /**
     * Call <code>public.pgp_armor_headers</code>.
     */
    public static Result<PgpArmorHeadersRecord> PGP_ARMOR_HEADERS(
        Configuration configuration
        , String __1
    ) {
        return configuration.dsl().selectFrom(PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
            __1
        )).fetch();
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(
        String __1
    ) {
        return PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
            __1
        );
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(
        Field<String> __1
    ) {
        return PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
            __1
        );
    }

    /**
     * The table <code>public.retired_user_stats</code>.
     */
    public static final RetiredUserStats RETIRED_USER_STATS = RetiredUserStats.RETIRED_USER_STATS;

    /**
     * The table <code>public.system_users</code>.
     */
    public static final SystemUsers SYSTEM_USERS = SystemUsers.SYSTEM_USERS;

    /**
     * The table <code>public.teams</code>.
     */
    public static final Teams TEAMS = Teams.TEAMS;

    /**
     * The table <code>public.user_changes</code>.
     */
    public static final UserChanges USER_CHANGES = UserChanges.USER_CHANGES;

    /**
     * The table <code>public.user_initial_stats</code>.
     */
    public static final UserInitialStats USER_INITIAL_STATS = UserInitialStats.USER_INITIAL_STATS;

    /**
     * The table <code>public.user_offset_tc_stats</code>.
     */
    public static final UserOffsetTcStats USER_OFFSET_TC_STATS = UserOffsetTcStats.USER_OFFSET_TC_STATS;

    /**
     * The table <code>public.user_tc_stats_hourly</code>.
     */
    public static final UserTcStatsHourly USER_TC_STATS_HOURLY = UserTcStatsHourly.USER_TC_STATS_HOURLY;

    /**
     * The table <code>public.user_total_stats</code>.
     */
    public static final UserTotalStats USER_TOTAL_STATS = UserTotalStats.USER_TOTAL_STATS;

    /**
     * The table <code>public.users</code>.
     */
    public static final Users USERS = Users.USERS;
}
