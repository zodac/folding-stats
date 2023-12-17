/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.zodac.folding.db.postgres.gen.Indexes;
import me.zodac.folding.db.postgres.gen.Keys;
import me.zodac.folding.db.postgres.gen.Public;
import me.zodac.folding.db.postgres.gen.tables.Users.UsersPath;
import me.zodac.folding.db.postgres.gen.tables.records.UserTcStatsHourlyRecord;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
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
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class UserTcStatsHourly extends TableImpl<UserTcStatsHourlyRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.user_tc_stats_hourly</code>
     */
    public static final UserTcStatsHourly USER_TC_STATS_HOURLY = new UserTcStatsHourly();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UserTcStatsHourlyRecord> getRecordType() {
        return UserTcStatsHourlyRecord.class;
    }

    /**
     * The column <code>public.user_tc_stats_hourly.user_id</code>.
     */
    public final TableField<UserTcStatsHourlyRecord, Integer> USER_ID = createField(DSL.name("user_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.user_tc_stats_hourly.utc_timestamp</code>.
     */
    public final TableField<UserTcStatsHourlyRecord, LocalDateTime> UTC_TIMESTAMP = createField(DSL.name("utc_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.user_tc_stats_hourly.tc_points</code>.
     */
    public final TableField<UserTcStatsHourlyRecord, Long> TC_POINTS = createField(DSL.name("tc_points"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.user_tc_stats_hourly.tc_points_multiplied</code>.
     */
    public final TableField<UserTcStatsHourlyRecord, Long> TC_POINTS_MULTIPLIED = createField(DSL.name("tc_points_multiplied"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.user_tc_stats_hourly.tc_units</code>.
     */
    public final TableField<UserTcStatsHourlyRecord, Integer> TC_UNITS = createField(DSL.name("tc_units"), SQLDataType.INTEGER.nullable(false), this, "");

    private UserTcStatsHourly(Name alias, Table<UserTcStatsHourlyRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private UserTcStatsHourly(Name alias, Table<UserTcStatsHourlyRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.user_tc_stats_hourly</code> table
     * reference
     */
    public UserTcStatsHourly(String alias) {
        this(DSL.name(alias), USER_TC_STATS_HOURLY);
    }

    /**
     * Create an aliased <code>public.user_tc_stats_hourly</code> table
     * reference
     */
    public UserTcStatsHourly(Name alias) {
        this(alias, USER_TC_STATS_HOURLY);
    }

    /**
     * Create a <code>public.user_tc_stats_hourly</code> table reference
     */
    public UserTcStatsHourly() {
        this(DSL.name("user_tc_stats_hourly"), null);
    }

    public <O extends Record> UserTcStatsHourly(Table<O> path, ForeignKey<O, UserTcStatsHourlyRecord> childPath, InverseForeignKey<O, UserTcStatsHourlyRecord> parentPath) {
        super(path, childPath, parentPath, USER_TC_STATS_HOURLY);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class UserTcStatsHourlyPath extends UserTcStatsHourly implements Path<UserTcStatsHourlyRecord> {
        public <O extends Record> UserTcStatsHourlyPath(Table<O> path, ForeignKey<O, UserTcStatsHourlyRecord> childPath, InverseForeignKey<O, UserTcStatsHourlyRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private UserTcStatsHourlyPath(Name alias, Table<UserTcStatsHourlyRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public UserTcStatsHourlyPath as(String alias) {
            return new UserTcStatsHourlyPath(DSL.name(alias), this);
        }

        @Override
        public UserTcStatsHourlyPath as(Name alias) {
            return new UserTcStatsHourlyPath(alias, this);
        }

        @Override
        public UserTcStatsHourlyPath as(Table<?> alias) {
            return new UserTcStatsHourlyPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.INDEX_USER_TC_STATS_HOURLY);
    }

    @Override
    public UniqueKey<UserTcStatsHourlyRecord> getPrimaryKey() {
        return Keys.USER_TC_STATS_HOURLY_PKEY;
    }

    @Override
    public List<ForeignKey<UserTcStatsHourlyRecord, ?>> getReferences() {
        return Arrays.asList(Keys.USER_TC_STATS_HOURLY__FK_USER_ID);
    }

    private transient UsersPath _users;

    /**
     * Get the implicit join path to the <code>public.users</code> table.
     */
    public UsersPath users() {
        if (_users == null)
            _users = new UsersPath(this, Keys.USER_TC_STATS_HOURLY__FK_USER_ID, null);

        return _users;
    }

    @Override
    public UserTcStatsHourly as(String alias) {
        return new UserTcStatsHourly(DSL.name(alias), this);
    }

    @Override
    public UserTcStatsHourly as(Name alias) {
        return new UserTcStatsHourly(alias, this);
    }

    @Override
    public UserTcStatsHourly as(Table<?> alias) {
        return new UserTcStatsHourly(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTcStatsHourly rename(String name) {
        return new UserTcStatsHourly(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTcStatsHourly rename(Name name) {
        return new UserTcStatsHourly(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserTcStatsHourly rename(Table<?> name) {
        return new UserTcStatsHourly(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTcStatsHourly where(Condition condition) {
        return new UserTcStatsHourly(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTcStatsHourly where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTcStatsHourly where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTcStatsHourly where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTcStatsHourly where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTcStatsHourly where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTcStatsHourly where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserTcStatsHourly where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTcStatsHourly whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserTcStatsHourly whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
