/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables.records;


import me.zodac.folding.db.postgres.gen.tables.RetiredUserStats;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
public class RetiredUserStatsRecord extends UpdatableRecordImpl<RetiredUserStatsRecord> implements Record8<Integer, Integer, Integer, String, LocalDateTime, Long, Long, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.retired_user_stats.retired_user_id</code>.
     */
    public void setRetiredUserId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.retired_user_id</code>.
     */
    public Integer getRetiredUserId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.retired_user_stats.team_id</code>.
     */
    public void setTeamId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.team_id</code>.
     */
    public Integer getTeamId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.retired_user_stats.user_id</code>.
     */
    public void setUserId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.user_id</code>.
     */
    public Integer getUserId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>public.retired_user_stats.display_username</code>.
     */
    public void setDisplayUsername(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.display_username</code>.
     */
    public String getDisplayUsername() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.retired_user_stats.utc_timestamp</code>.
     */
    public void setUtcTimestamp(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.utc_timestamp</code>.
     */
    public LocalDateTime getUtcTimestamp() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>public.retired_user_stats.final_points</code>.
     */
    public void setFinalPoints(Long value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.final_points</code>.
     */
    public Long getFinalPoints() {
        return (Long) get(5);
    }

    /**
     * Setter for <code>public.retired_user_stats.final_multiplied_points</code>.
     */
    public void setFinalMultipliedPoints(Long value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.final_multiplied_points</code>.
     */
    public Long getFinalMultipliedPoints() {
        return (Long) get(6);
    }

    /**
     * Setter for <code>public.retired_user_stats.final_units</code>.
     */
    public void setFinalUnits(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.retired_user_stats.final_units</code>.
     */
    public Integer getFinalUnits() {
        return (Integer) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<Integer, Integer, Integer, String, LocalDateTime, Long, Long, Integer> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<Integer, Integer, Integer, String, LocalDateTime, Long, Long, Integer> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RetiredUserStats.RETIRED_USER_STATS.RETIRED_USER_ID;
    }

    @Override
    public Field<Integer> field2() {
        return RetiredUserStats.RETIRED_USER_STATS.TEAM_ID;
    }

    @Override
    public Field<Integer> field3() {
        return RetiredUserStats.RETIRED_USER_STATS.USER_ID;
    }

    @Override
    public Field<String> field4() {
        return RetiredUserStats.RETIRED_USER_STATS.DISPLAY_USERNAME;
    }

    @Override
    public Field<LocalDateTime> field5() {
        return RetiredUserStats.RETIRED_USER_STATS.UTC_TIMESTAMP;
    }

    @Override
    public Field<Long> field6() {
        return RetiredUserStats.RETIRED_USER_STATS.FINAL_POINTS;
    }

    @Override
    public Field<Long> field7() {
        return RetiredUserStats.RETIRED_USER_STATS.FINAL_MULTIPLIED_POINTS;
    }

    @Override
    public Field<Integer> field8() {
        return RetiredUserStats.RETIRED_USER_STATS.FINAL_UNITS;
    }

    @Override
    public Integer component1() {
        return getRetiredUserId();
    }

    @Override
    public Integer component2() {
        return getTeamId();
    }

    @Override
    public Integer component3() {
        return getUserId();
    }

    @Override
    public String component4() {
        return getDisplayUsername();
    }

    @Override
    public LocalDateTime component5() {
        return getUtcTimestamp();
    }

    @Override
    public Long component6() {
        return getFinalPoints();
    }

    @Override
    public Long component7() {
        return getFinalMultipliedPoints();
    }

    @Override
    public Integer component8() {
        return getFinalUnits();
    }

    @Override
    public Integer value1() {
        return getRetiredUserId();
    }

    @Override
    public Integer value2() {
        return getTeamId();
    }

    @Override
    public Integer value3() {
        return getUserId();
    }

    @Override
    public String value4() {
        return getDisplayUsername();
    }

    @Override
    public LocalDateTime value5() {
        return getUtcTimestamp();
    }

    @Override
    public Long value6() {
        return getFinalPoints();
    }

    @Override
    public Long value7() {
        return getFinalMultipliedPoints();
    }

    @Override
    public Integer value8() {
        return getFinalUnits();
    }

    @Override
    public RetiredUserStatsRecord value1(Integer value) {
        setRetiredUserId(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value2(Integer value) {
        setTeamId(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value3(Integer value) {
        setUserId(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value4(String value) {
        setDisplayUsername(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value5(LocalDateTime value) {
        setUtcTimestamp(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value6(Long value) {
        setFinalPoints(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value7(Long value) {
        setFinalMultipliedPoints(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord value8(Integer value) {
        setFinalUnits(value);
        return this;
    }

    @Override
    public RetiredUserStatsRecord values(Integer value1, Integer value2, Integer value3, String value4, LocalDateTime value5, Long value6, Long value7, Integer value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RetiredUserStatsRecord
     */
    public RetiredUserStatsRecord() {
        super(RetiredUserStats.RETIRED_USER_STATS);
    }

    /**
     * Create a detached, initialised RetiredUserStatsRecord
     */
    public RetiredUserStatsRecord(Integer retiredUserId, Integer teamId, Integer userId, String displayUsername, LocalDateTime utcTimestamp, Long finalPoints, Long finalMultipliedPoints, Integer finalUnits) {
        super(RetiredUserStats.RETIRED_USER_STATS);

        setRetiredUserId(retiredUserId);
        setTeamId(teamId);
        setUserId(userId);
        setDisplayUsername(displayUsername);
        setUtcTimestamp(utcTimestamp);
        setFinalPoints(finalPoints);
        setFinalMultipliedPoints(finalMultipliedPoints);
        setFinalUnits(finalUnits);
    }
}