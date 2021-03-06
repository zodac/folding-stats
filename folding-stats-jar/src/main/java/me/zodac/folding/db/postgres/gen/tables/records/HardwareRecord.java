/*
 * This file is generated by jOOQ.
 */
package me.zodac.folding.db.postgres.gen.tables.records;


import java.math.BigDecimal;

import me.zodac.folding.db.postgres.gen.tables.Hardware;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class HardwareRecord extends UpdatableRecordImpl<HardwareRecord> implements Record7<Integer, String, String, String, String, BigDecimal, BigDecimal> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.hardware.hardware_id</code>.
     */
    public void setHardwareId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.hardware.hardware_id</code>.
     */
    public Integer getHardwareId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.hardware.hardware_name</code>.
     */
    public void setHardwareName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.hardware.hardware_name</code>.
     */
    public String getHardwareName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.hardware.display_name</code>.
     */
    public void setDisplayName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.hardware.display_name</code>.
     */
    public String getDisplayName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.hardware.hardware_make</code>.
     */
    public void setHardwareMake(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.hardware.hardware_make</code>.
     */
    public String getHardwareMake() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.hardware.hardware_type</code>.
     */
    public void setHardwareType(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.hardware.hardware_type</code>.
     */
    public String getHardwareType() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.hardware.multiplier</code>.
     */
    public void setMultiplier(BigDecimal value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.hardware.multiplier</code>.
     */
    public BigDecimal getMultiplier() {
        return (BigDecimal) get(5);
    }

    /**
     * Setter for <code>public.hardware.average_ppd</code>.
     */
    public void setAveragePpd(BigDecimal value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.hardware.average_ppd</code>.
     */
    public BigDecimal getAveragePpd() {
        return (BigDecimal) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<Integer, String, String, String, String, BigDecimal, BigDecimal> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<Integer, String, String, String, String, BigDecimal, BigDecimal> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return Hardware.HARDWARE.HARDWARE_ID;
    }

    @Override
    public Field<String> field2() {
        return Hardware.HARDWARE.HARDWARE_NAME;
    }

    @Override
    public Field<String> field3() {
        return Hardware.HARDWARE.DISPLAY_NAME;
    }

    @Override
    public Field<String> field4() {
        return Hardware.HARDWARE.HARDWARE_MAKE;
    }

    @Override
    public Field<String> field5() {
        return Hardware.HARDWARE.HARDWARE_TYPE;
    }

    @Override
    public Field<BigDecimal> field6() {
        return Hardware.HARDWARE.MULTIPLIER;
    }

    @Override
    public Field<BigDecimal> field7() {
        return Hardware.HARDWARE.AVERAGE_PPD;
    }

    @Override
    public Integer component1() {
        return getHardwareId();
    }

    @Override
    public String component2() {
        return getHardwareName();
    }

    @Override
    public String component3() {
        return getDisplayName();
    }

    @Override
    public String component4() {
        return getHardwareMake();
    }

    @Override
    public String component5() {
        return getHardwareType();
    }

    @Override
    public BigDecimal component6() {
        return getMultiplier();
    }

    @Override
    public BigDecimal component7() {
        return getAveragePpd();
    }

    @Override
    public Integer value1() {
        return getHardwareId();
    }

    @Override
    public String value2() {
        return getHardwareName();
    }

    @Override
    public String value3() {
        return getDisplayName();
    }

    @Override
    public String value4() {
        return getHardwareMake();
    }

    @Override
    public String value5() {
        return getHardwareType();
    }

    @Override
    public BigDecimal value6() {
        return getMultiplier();
    }

    @Override
    public BigDecimal value7() {
        return getAveragePpd();
    }

    @Override
    public HardwareRecord value1(Integer value) {
        setHardwareId(value);
        return this;
    }

    @Override
    public HardwareRecord value2(String value) {
        setHardwareName(value);
        return this;
    }

    @Override
    public HardwareRecord value3(String value) {
        setDisplayName(value);
        return this;
    }

    @Override
    public HardwareRecord value4(String value) {
        setHardwareMake(value);
        return this;
    }

    @Override
    public HardwareRecord value5(String value) {
        setHardwareType(value);
        return this;
    }

    @Override
    public HardwareRecord value6(BigDecimal value) {
        setMultiplier(value);
        return this;
    }

    @Override
    public HardwareRecord value7(BigDecimal value) {
        setAveragePpd(value);
        return this;
    }

    @Override
    public HardwareRecord values(Integer value1, String value2, String value3, String value4, String value5, BigDecimal value6, BigDecimal value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached HardwareRecord
     */
    public HardwareRecord() {
        super(Hardware.HARDWARE);
    }

    /**
     * Create a detached, initialised HardwareRecord
     */
    public HardwareRecord(Integer hardwareId, String hardwareName, String displayName, String hardwareMake, String hardwareType, BigDecimal multiplier, BigDecimal averagePpd) {
        super(Hardware.HARDWARE);

        setHardwareId(hardwareId);
        setHardwareName(hardwareName);
        setDisplayName(displayName);
        setHardwareMake(hardwareMake);
        setHardwareType(hardwareType);
        setMultiplier(multiplier);
        setAveragePpd(averagePpd);
    }
}
