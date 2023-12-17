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
import me.zodac.folding.db.postgres.gen.tables.Teams.TeamsPath;
import me.zodac.folding.db.postgres.gen.tables.records.RetiredUserStatsRecord;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
public class RetiredUserStats extends TableImpl<RetiredUserStatsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.retired_user_stats</code>
     */
    public static final RetiredUserStats RETIRED_USER_STATS = new RetiredUserStats();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RetiredUserStatsRecord> getRecordType() {
        return RetiredUserStatsRecord.class;
    }

    /**
     * The column <code>public.retired_user_stats.retired_user_id</code>.
     */
    public final TableField<RetiredUserStatsRecord, Integer> RETIRED_USER_ID = createField(DSL.name("retired_user_id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.retired_user_stats.team_id</code>.
     */
    public final TableField<RetiredUserStatsRecord, Integer> TEAM_ID = createField(DSL.name("team_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.retired_user_stats.user_id</code>.
     */
    public final TableField<RetiredUserStatsRecord, Integer> USER_ID = createField(DSL.name("user_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.retired_user_stats.display_username</code>.
     */
    public final TableField<RetiredUserStatsRecord, String> DISPLAY_USERNAME = createField(DSL.name("display_username"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.retired_user_stats.utc_timestamp</code>.
     */
    public final TableField<RetiredUserStatsRecord, LocalDateTime> UTC_TIMESTAMP = createField(DSL.name("utc_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.retired_user_stats.final_points</code>.
     */
    public final TableField<RetiredUserStatsRecord, Long> FINAL_POINTS = createField(DSL.name("final_points"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column
     * <code>public.retired_user_stats.final_multiplied_points</code>.
     */
    public final TableField<RetiredUserStatsRecord, Long> FINAL_MULTIPLIED_POINTS = createField(DSL.name("final_multiplied_points"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.retired_user_stats.final_units</code>.
     */
    public final TableField<RetiredUserStatsRecord, Integer> FINAL_UNITS = createField(DSL.name("final_units"), SQLDataType.INTEGER.nullable(false), this, "");

    private RetiredUserStats(Name alias, Table<RetiredUserStatsRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private RetiredUserStats(Name alias, Table<RetiredUserStatsRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.retired_user_stats</code> table reference
     */
    public RetiredUserStats(String alias) {
        this(DSL.name(alias), RETIRED_USER_STATS);
    }

    /**
     * Create an aliased <code>public.retired_user_stats</code> table reference
     */
    public RetiredUserStats(Name alias) {
        this(alias, RETIRED_USER_STATS);
    }

    /**
     * Create a <code>public.retired_user_stats</code> table reference
     */
    public RetiredUserStats() {
        this(DSL.name("retired_user_stats"), null);
    }

    public <O extends Record> RetiredUserStats(Table<O> path, ForeignKey<O, RetiredUserStatsRecord> childPath, InverseForeignKey<O, RetiredUserStatsRecord> parentPath) {
        super(path, childPath, parentPath, RETIRED_USER_STATS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class RetiredUserStatsPath extends RetiredUserStats implements Path<RetiredUserStatsRecord> {
        public <O extends Record> RetiredUserStatsPath(Table<O> path, ForeignKey<O, RetiredUserStatsRecord> childPath, InverseForeignKey<O, RetiredUserStatsRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private RetiredUserStatsPath(Name alias, Table<RetiredUserStatsRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public RetiredUserStatsPath as(String alias) {
            return new RetiredUserStatsPath(DSL.name(alias), this);
        }

        @Override
        public RetiredUserStatsPath as(Name alias) {
            return new RetiredUserStatsPath(alias, this);
        }

        @Override
        public RetiredUserStatsPath as(Table<?> alias) {
            return new RetiredUserStatsPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.INDEX_RETIRED_USER_STATS);
    }

    @Override
    public Identity<RetiredUserStatsRecord, Integer> getIdentity() {
        return (Identity<RetiredUserStatsRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<RetiredUserStatsRecord> getPrimaryKey() {
        return Keys.RETIRED_USER_STATS_PKEY;
    }

    @Override
    public List<UniqueKey<RetiredUserStatsRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.RETIRED_USER_STATS_USER_ID_KEY);
    }

    @Override
    public List<ForeignKey<RetiredUserStatsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.RETIRED_USER_STATS__FK_TEAM_ID);
    }

    private transient TeamsPath _teams;

    /**
     * Get the implicit join path to the <code>public.teams</code> table.
     */
    public TeamsPath teams() {
        if (_teams == null)
            _teams = new TeamsPath(this, Keys.RETIRED_USER_STATS__FK_TEAM_ID, null);

        return _teams;
    }

    @Override
    public RetiredUserStats as(String alias) {
        return new RetiredUserStats(DSL.name(alias), this);
    }

    @Override
    public RetiredUserStats as(Name alias) {
        return new RetiredUserStats(alias, this);
    }

    @Override
    public RetiredUserStats as(Table<?> alias) {
        return new RetiredUserStats(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public RetiredUserStats rename(String name) {
        return new RetiredUserStats(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RetiredUserStats rename(Name name) {
        return new RetiredUserStats(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public RetiredUserStats rename(Table<?> name) {
        return new RetiredUserStats(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RetiredUserStats where(Condition condition) {
        return new RetiredUserStats(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RetiredUserStats where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RetiredUserStats where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RetiredUserStats where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RetiredUserStats where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RetiredUserStats where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RetiredUserStats where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RetiredUserStats where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RetiredUserStats whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RetiredUserStats whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
