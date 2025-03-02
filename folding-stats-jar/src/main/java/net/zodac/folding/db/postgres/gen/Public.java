/*
 * This file is generated by jOOQ.
 */
package net.zodac.folding.db.postgres.gen;


import java.util.Arrays;
import java.util.List;

import net.zodac.folding.db.postgres.gen.tables.Hardware;
import net.zodac.folding.db.postgres.gen.tables.MonthlyResults;
import net.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders;
import net.zodac.folding.db.postgres.gen.tables.RetiredUserStats;
import net.zodac.folding.db.postgres.gen.tables.SystemUsers;
import net.zodac.folding.db.postgres.gen.tables.Teams;
import net.zodac.folding.db.postgres.gen.tables.UserChanges;
import net.zodac.folding.db.postgres.gen.tables.UserInitialStats;
import net.zodac.folding.db.postgres.gen.tables.UserOffsetTcStats;
import net.zodac.folding.db.postgres.gen.tables.UserTcStatsHourly;
import net.zodac.folding.db.postgres.gen.tables.UserTotalStats;
import net.zodac.folding.db.postgres.gen.tables.Users;
import net.zodac.folding.db.postgres.gen.tables.records.PgpArmorHeadersRecord;

import org.jooq.Catalog;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SchemaImpl;


/**
 * standard public schema
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.hardware</code>.
     */
    public final Hardware HARDWARE = Hardware.HARDWARE;

    /**
     * The table <code>public.monthly_results</code>.
     */
    public final MonthlyResults MONTHLY_RESULTS = MonthlyResults.MONTHLY_RESULTS;

    /**
     * The table <code>public.pgp_armor_headers</code>.
     */
    public final PgpArmorHeaders PGP_ARMOR_HEADERS = PgpArmorHeaders.PGP_ARMOR_HEADERS;

    /**
     * Call <code>public.pgp_armor_headers</code>.
     */
    public static Result<PgpArmorHeadersRecord> PGP_ARMOR_HEADERS(
          Configuration configuration
        , String __1
    ) {
        return configuration.dsl().selectFrom(net.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
              __1
        )).fetch();
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(
          String __1
    ) {
        return net.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
            __1
        );
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(
          Field<String> __1
    ) {
        return net.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
            __1
        );
    }

    /**
     * The table <code>public.retired_user_stats</code>.
     */
    public final RetiredUserStats RETIRED_USER_STATS = RetiredUserStats.RETIRED_USER_STATS;

    /**
     * The table <code>public.system_users</code>.
     */
    public final SystemUsers SYSTEM_USERS = SystemUsers.SYSTEM_USERS;

    /**
     * The table <code>public.teams</code>.
     */
    public final Teams TEAMS = Teams.TEAMS;

    /**
     * The table <code>public.user_changes</code>.
     */
    public final UserChanges USER_CHANGES = UserChanges.USER_CHANGES;

    /**
     * The table <code>public.user_initial_stats</code>.
     */
    public final UserInitialStats USER_INITIAL_STATS = UserInitialStats.USER_INITIAL_STATS;

    /**
     * The table <code>public.user_offset_tc_stats</code>.
     */
    public final UserOffsetTcStats USER_OFFSET_TC_STATS = UserOffsetTcStats.USER_OFFSET_TC_STATS;

    /**
     * The table <code>public.user_tc_stats_hourly</code>.
     */
    public final UserTcStatsHourly USER_TC_STATS_HOURLY = UserTcStatsHourly.USER_TC_STATS_HOURLY;

    /**
     * The table <code>public.user_total_stats</code>.
     */
    public final UserTotalStats USER_TOTAL_STATS = UserTotalStats.USER_TOTAL_STATS;

    /**
     * The table <code>public.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * No further instances allowed
     */
    private Public() {
        super(DSL.name("public"), null, DSL.comment("standard public schema"));
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Hardware.HARDWARE,
            MonthlyResults.MONTHLY_RESULTS,
            PgpArmorHeaders.PGP_ARMOR_HEADERS,
            RetiredUserStats.RETIRED_USER_STATS,
            SystemUsers.SYSTEM_USERS,
            Teams.TEAMS,
            UserChanges.USER_CHANGES,
            UserInitialStats.USER_INITIAL_STATS,
            UserOffsetTcStats.USER_OFFSET_TC_STATS,
            UserTcStatsHourly.USER_TC_STATS_HOURLY,
            UserTotalStats.USER_TOTAL_STATS,
            Users.USERS
        );
    }
}
