/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables.records;


import me.zodac.folding.db.postgres.gen.tables.Teams;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
public class TeamsRecord extends UpdatableRecordImpl<TeamsRecord> implements Record4<Integer, String, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.teams.team_id</code>.
     */
    public void setTeamId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.teams.team_id</code>.
     */
    public Integer getTeamId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.teams.team_name</code>.
     */
    public void setTeamName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.teams.team_name</code>.
     */
    public String getTeamName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.teams.team_description</code>.
     */
    public void setTeamDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.teams.team_description</code>.
     */
    public String getTeamDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.teams.forum_link</code>.
     */
    public void setForumLink(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.teams.forum_link</code>.
     */
    public String getForumLink() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Integer, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return Teams.TEAMS.TEAM_ID;
    }

    @Override
    public Field<String> field2() {
        return Teams.TEAMS.TEAM_NAME;
    }

    @Override
    public Field<String> field3() {
        return Teams.TEAMS.TEAM_DESCRIPTION;
    }

    @Override
    public Field<String> field4() {
        return Teams.TEAMS.FORUM_LINK;
    }

    @Override
    public Integer component1() {
        return getTeamId();
    }

    @Override
    public String component2() {
        return getTeamName();
    }

    @Override
    public String component3() {
        return getTeamDescription();
    }

    @Override
    public String component4() {
        return getForumLink();
    }

    @Override
    public Integer value1() {
        return getTeamId();
    }

    @Override
    public String value2() {
        return getTeamName();
    }

    @Override
    public String value3() {
        return getTeamDescription();
    }

    @Override
    public String value4() {
        return getForumLink();
    }

    @Override
    public TeamsRecord value1(Integer value) {
        setTeamId(value);
        return this;
    }

    @Override
    public TeamsRecord value2(String value) {
        setTeamName(value);
        return this;
    }

    @Override
    public TeamsRecord value3(String value) {
        setTeamDescription(value);
        return this;
    }

    @Override
    public TeamsRecord value4(String value) {
        setForumLink(value);
        return this;
    }

    @Override
    public TeamsRecord values(Integer value1, String value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TeamsRecord
     */
    public TeamsRecord() {
        super(Teams.TEAMS);
    }

    /**
     * Create a detached, initialised TeamsRecord
     */
    public TeamsRecord(Integer teamId, String teamName, String teamDescription, String forumLink) {
        super(Teams.TEAMS);

        setTeamId(teamId);
        setTeamName(teamName);
        setTeamDescription(teamDescription);
        setForumLink(forumLink);
    }
}