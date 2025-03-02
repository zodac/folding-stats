/*
 * This file is generated by jOOQ.
 */
package net.zodac.folding.db.postgres.gen.tables.records;


import java.time.LocalDateTime;

import net.zodac.folding.db.postgres.gen.tables.MonthlyResults;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class MonthlyResultsRecord extends UpdatableRecordImpl<MonthlyResultsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.monthly_results.utc_timestamp</code>.
     */
    public void setUtcTimestamp(LocalDateTime value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.monthly_results.utc_timestamp</code>.
     */
    public LocalDateTime getUtcTimestamp() {
        return (LocalDateTime) get(0);
    }

    /**
     * Setter for <code>public.monthly_results.json_result</code>.
     */
    public void setJsonResult(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.monthly_results.json_result</code>.
     */
    public String getJsonResult() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<LocalDateTime> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MonthlyResultsRecord
     */
    public MonthlyResultsRecord() {
        super(MonthlyResults.MONTHLY_RESULTS);
    }

    /**
     * Create a detached, initialised MonthlyResultsRecord
     */
    public MonthlyResultsRecord(LocalDateTime utcTimestamp, String jsonResult) {
        super(MonthlyResults.MONTHLY_RESULTS);

        setUtcTimestamp(utcTimestamp);
        setJsonResult(jsonResult);
        resetTouchedOnNotNull();
    }
}
