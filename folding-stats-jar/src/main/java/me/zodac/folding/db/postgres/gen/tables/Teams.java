/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables;


import java.util.Arrays;
import java.util.List;

import me.zodac.folding.db.postgres.gen.Indexes;
import me.zodac.folding.db.postgres.gen.Keys;
import me.zodac.folding.db.postgres.gen.Public;
import me.zodac.folding.db.postgres.gen.tables.records.TeamsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
public class Teams extends TableImpl<TeamsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.teams</code>
     */
    public static final Teams TEAMS = new Teams();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TeamsRecord> getRecordType() {
        return TeamsRecord.class;
    }

    /**
     * The column <code>public.teams.team_id</code>.
     */
    public final TableField<TeamsRecord, Integer> TEAM_ID = createField(DSL.name("team_id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.teams.team_name</code>.
     */
    public final TableField<TeamsRecord, String> TEAM_NAME = createField(DSL.name("team_name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.teams.team_description</code>.
     */
    public final TableField<TeamsRecord, String> TEAM_DESCRIPTION = createField(DSL.name("team_description"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.teams.forum_link</code>.
     */
    public final TableField<TeamsRecord, String> FORUM_LINK = createField(DSL.name("forum_link"), SQLDataType.CLOB, this, "");

    private Teams(Name alias, Table<TeamsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Teams(Name alias, Table<TeamsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.teams</code> table reference
     */
    public Teams(String alias) {
        this(DSL.name(alias), TEAMS);
    }

    /**
     * Create an aliased <code>public.teams</code> table reference
     */
    public Teams(Name alias) {
        this(alias, TEAMS);
    }

    /**
     * Create a <code>public.teams</code> table reference
     */
    public Teams() {
        this(DSL.name("teams"), null);
    }

    public <O extends Record> Teams(Table<O> child, ForeignKey<O, TeamsRecord> key) {
        super(child, key, TEAMS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.INDEX_TEAM_ID);
    }

    @Override
    public Identity<TeamsRecord, Integer> getIdentity() {
        return (Identity<TeamsRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<TeamsRecord> getPrimaryKey() {
        return Keys.TEAMS_PKEY;
    }

    @Override
    public List<UniqueKey<TeamsRecord>> getKeys() {
        return Arrays.<UniqueKey<TeamsRecord>>asList(Keys.TEAMS_PKEY, Keys.TEAMS_TEAM_NAME_KEY);
    }

    @Override
    public Teams as(String alias) {
        return new Teams(DSL.name(alias), this);
    }

    @Override
    public Teams as(Name alias) {
        return new Teams(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Teams rename(String name) {
        return new Teams(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Teams rename(Name name) {
        return new Teams(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
