/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen;


import me.zodac.folding.db.postgres.gen.tables.Hardware;
import me.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders;
import me.zodac.folding.db.postgres.gen.tables.RetiredUserStats;
import me.zodac.folding.db.postgres.gen.tables.SystemUsers;
import me.zodac.folding.db.postgres.gen.tables.Teams;
import me.zodac.folding.db.postgres.gen.tables.UserInitialStats;
import me.zodac.folding.db.postgres.gen.tables.UserOffsetTcStats;
import me.zodac.folding.db.postgres.gen.tables.UserTcStatsHourly;
import me.zodac.folding.db.postgres.gen.tables.UserTotalStats;
import me.zodac.folding.db.postgres.gen.tables.Users;
import me.zodac.folding.db.postgres.gen.tables.records.PgpArmorHeadersRecord;
import org.jooq.Catalog;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
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
        return configuration.dsl().selectFrom(me.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
                __1
        )).fetch();
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(
            String __1
    ) {
        return me.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
                __1
        );
    }

    /**
     * Get <code>public.pgp_armor_headers</code> as a table.
     */
    public static PgpArmorHeaders PGP_ARMOR_HEADERS(
            Field<String> __1
    ) {
        return me.zodac.folding.db.postgres.gen.tables.PgpArmorHeaders.PGP_ARMOR_HEADERS.call(
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
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.<Sequence<?>>asList(
                Sequences.HARDWARE_HARDWARE_ID_SEQ,
                Sequences.RETIRED_USER_STATS_RETIRED_USER_ID_SEQ,
                Sequences.TEAMS_TEAM_ID_SEQ,
                Sequences.USERS_USER_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
                Hardware.HARDWARE,
                PgpArmorHeaders.PGP_ARMOR_HEADERS,
                RetiredUserStats.RETIRED_USER_STATS,
                SystemUsers.SYSTEM_USERS,
                Teams.TEAMS,
                UserInitialStats.USER_INITIAL_STATS,
                UserOffsetTcStats.USER_OFFSET_TC_STATS,
                UserTcStatsHourly.USER_TC_STATS_HOURLY,
                UserTotalStats.USER_TOTAL_STATS,
                Users.USERS);
    }
}
