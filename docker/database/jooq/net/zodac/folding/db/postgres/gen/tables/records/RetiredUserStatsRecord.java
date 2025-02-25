/*
 * This file is generated by jOOQ.
 */
package net.zodac.folding.db.postgres.gen.tables.records;


import java.time.LocalDateTime;

import net.zodac.folding.db.postgres.gen.tables.RetiredUserStats;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class RetiredUserStatsRecord extends UpdatableRecordImpl<RetiredUserStatsRecord> {

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
     * Setter for
     * <code>public.retired_user_stats.final_multiplied_points</code>.
     */
    public void setFinalMultipliedPoints(Long value) {
        set(6, value);
    }

    /**
     * Getter for
     * <code>public.retired_user_stats.final_multiplied_points</code>.
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
        resetTouchedOnNotNull();
    }
}
