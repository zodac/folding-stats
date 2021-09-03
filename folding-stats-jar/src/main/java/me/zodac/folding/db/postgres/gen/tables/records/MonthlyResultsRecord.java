/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables.records;


import java.time.LocalDateTime;

import me.zodac.folding.db.postgres.gen.tables.MonthlyResults;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MonthlyResultsRecord extends UpdatableRecordImpl<MonthlyResultsRecord> implements Record2<LocalDateTime, String> {

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
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<LocalDateTime, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<LocalDateTime, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<LocalDateTime> field1() {
        return MonthlyResults.MONTHLY_RESULTS.UTC_TIMESTAMP;
    }

    @Override
    public Field<String> field2() {
        return MonthlyResults.MONTHLY_RESULTS.JSON_RESULT;
    }

    @Override
    public LocalDateTime component1() {
        return getUtcTimestamp();
    }

    @Override
    public String component2() {
        return getJsonResult();
    }

    @Override
    public LocalDateTime value1() {
        return getUtcTimestamp();
    }

    @Override
    public String value2() {
        return getJsonResult();
    }

    @Override
    public MonthlyResultsRecord value1(LocalDateTime value) {
        setUtcTimestamp(value);
        return this;
    }

    @Override
    public MonthlyResultsRecord value2(String value) {
        setJsonResult(value);
        return this;
    }

    @Override
    public MonthlyResultsRecord values(LocalDateTime value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
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
    }
}