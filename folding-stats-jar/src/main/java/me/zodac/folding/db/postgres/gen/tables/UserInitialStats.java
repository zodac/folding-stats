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
import me.zodac.folding.db.postgres.gen.tables.records.UserInitialStatsRecord;

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
public class UserInitialStats extends TableImpl<UserInitialStatsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.user_initial_stats</code>
     */
    public static final UserInitialStats USER_INITIAL_STATS = new UserInitialStats();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UserInitialStatsRecord> getRecordType() {
        return UserInitialStatsRecord.class;
    }

    /**
     * The column <code>public.user_initial_stats.user_id</code>.
     */
    public final TableField<UserInitialStatsRecord, Integer> USER_ID = createField(DSL.name("user_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.user_initial_stats.utc_timestamp</code>.
     */
    public final TableField<UserInitialStatsRecord, LocalDateTime> UTC_TIMESTAMP = createField(DSL.name("utc_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.user_initial_stats.initial_points</code>.
     */
    public final TableField<UserInitialStatsRecord, Long> INITIAL_POINTS = createField(DSL.name("initial_points"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.user_initial_stats.initial_units</code>.
     */
    public final TableField<UserInitialStatsRecord, Integer> INITIAL_UNITS = createField(DSL.name("initial_units"), SQLDataType.INTEGER.nullable(false), this, "");

    private UserInitialStats(Name alias, Table<UserInitialStatsRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private UserInitialStats(Name alias, Table<UserInitialStatsRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.user_initial_stats</code> table reference
     */
    public UserInitialStats(String alias) {
        this(DSL.name(alias), USER_INITIAL_STATS);
    }

    /**
     * Create an aliased <code>public.user_initial_stats</code> table reference
     */
    public UserInitialStats(Name alias) {
        this(alias, USER_INITIAL_STATS);
    }

    /**
     * Create a <code>public.user_initial_stats</code> table reference
     */
    public UserInitialStats() {
        this(DSL.name("user_initial_stats"), null);
    }

    public <O extends Record> UserInitialStats(Table<O> path, ForeignKey<O, UserInitialStatsRecord> childPath, InverseForeignKey<O, UserInitialStatsRecord> parentPath) {
        super(path, childPath, parentPath, USER_INITIAL_STATS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class UserInitialStatsPath extends UserInitialStats implements Path<UserInitialStatsRecord> {
        public <O extends Record> UserInitialStatsPath(Table<O> path, ForeignKey<O, UserInitialStatsRecord> childPath, InverseForeignKey<O, UserInitialStatsRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private UserInitialStatsPath(Name alias, Table<UserInitialStatsRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public UserInitialStatsPath as(String alias) {
            return new UserInitialStatsPath(DSL.name(alias), this);
        }

        @Override
        public UserInitialStatsPath as(Name alias) {
            return new UserInitialStatsPath(alias, this);
        }

        @Override
        public UserInitialStatsPath as(Table<?> alias) {
            return new UserInitialStatsPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.INDEX_USER_INITIAL_STATS);
    }

    @Override
    public UniqueKey<UserInitialStatsRecord> getPrimaryKey() {
        return Keys.USER_INITIAL_STATS_PKEY;
    }

    @Override
    public List<ForeignKey<UserInitialStatsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.USER_INITIAL_STATS__FK_USER_ID);
    }

    private transient UsersPath _users;

    /**
     * Get the implicit join path to the <code>public.users</code> table.
     */
    public UsersPath users() {
        if (_users == null)
            _users = new UsersPath(this, Keys.USER_INITIAL_STATS__FK_USER_ID, null);

        return _users;
    }

    @Override
    public UserInitialStats as(String alias) {
        return new UserInitialStats(DSL.name(alias), this);
    }

    @Override
    public UserInitialStats as(Name alias) {
        return new UserInitialStats(alias, this);
    }

    @Override
    public UserInitialStats as(Table<?> alias) {
        return new UserInitialStats(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public UserInitialStats rename(String name) {
        return new UserInitialStats(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserInitialStats rename(Name name) {
        return new UserInitialStats(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserInitialStats rename(Table<?> name) {
        return new UserInitialStats(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserInitialStats where(Condition condition) {
        return new UserInitialStats(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserInitialStats where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserInitialStats where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserInitialStats where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserInitialStats where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserInitialStats where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserInitialStats where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserInitialStats where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserInitialStats whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserInitialStats whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
