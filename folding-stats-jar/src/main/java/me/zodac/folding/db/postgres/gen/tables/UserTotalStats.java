/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import me.zodac.folding.db.postgres.gen.Indexes;
import me.zodac.folding.db.postgres.gen.Keys;
import me.zodac.folding.db.postgres.gen.Public;
import me.zodac.folding.db.postgres.gen.tables.records.UserTotalStatsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserTotalStats extends TableImpl<UserTotalStatsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.user_total_stats</code>
     */
    public static final UserTotalStats USER_TOTAL_STATS = new UserTotalStats();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UserTotalStatsRecord> getRecordType() {
        return UserTotalStatsRecord.class;
    }

    /**
     * The column <code>public.user_total_stats.user_id</code>.
     */
    public final TableField<UserTotalStatsRecord, Integer> USER_ID = createField(DSL.name("user_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.user_total_stats.utc_timestamp</code>.
     */
    public final TableField<UserTotalStatsRecord, LocalDateTime> UTC_TIMESTAMP = createField(DSL.name("utc_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.user_total_stats.total_points</code>.
     */
    public final TableField<UserTotalStatsRecord, Long> TOTAL_POINTS = createField(DSL.name("total_points"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.user_total_stats.total_units</code>.
     */
    public final TableField<UserTotalStatsRecord, Integer> TOTAL_UNITS = createField(DSL.name("total_units"), SQLDataType.INTEGER.nullable(false), this, "");

    private UserTotalStats(Name alias, Table<UserTotalStatsRecord> aliased) {
        this(alias, aliased, null);
    }

    private UserTotalStats(Name alias, Table<UserTotalStatsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.user_total_stats</code> table reference
     */
    public UserTotalStats(String alias) {
        this(DSL.name(alias), USER_TOTAL_STATS);
    }

    /**
     * Create an aliased <code>public.user_total_stats</code> table reference
     */
    public UserTotalStats(Name alias) {
        this(alias, USER_TOTAL_STATS);
    }

    /**
     * Create a <code>public.user_total_stats</code> table reference
     */
    public UserTotalStats() {
        this(DSL.name("user_total_stats"), null);
    }

    public <O extends Record> UserTotalStats(Table<O> child, ForeignKey<O, UserTotalStatsRecord> key) {
        super(child, key, USER_TOTAL_STATS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.INDEX_USER_TOTAL_STATS);
    }

    @Override
    public UniqueKey<UserTotalStatsRecord> getPrimaryKey() {
        return Keys.USER_TOTAL_STATS_PKEY;
    }

    @Override
    public List<UniqueKey<UserTotalStatsRecord>> getKeys() {
        return Arrays.<UniqueKey<UserTotalStatsRecord>>asList(Keys.USER_TOTAL_STATS_PKEY);
    }

    @Override
    public List<ForeignKey<UserTotalStatsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<UserTotalStatsRecord, ?>>asList(Keys.USER_TOTAL_STATS__FK_USER_ID);
    }

    private transient Users _users;

    public Users users() {
        if (_users == null)
            _users = new Users(this, Keys.USER_TOTAL_STATS__FK_USER_ID);

        return _users;
    }

    @Override
    public UserTotalStats as(String alias) {
        return new UserTotalStats(DSL.name(alias), this);
    }

    @Override
    public UserTotalStats as(Name alias) {
        return new UserTotalStats(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTotalStats rename(String name) {
        return new UserTotalStats(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTotalStats rename(Name name) {
        return new UserTotalStats(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, LocalDateTime, Long, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
