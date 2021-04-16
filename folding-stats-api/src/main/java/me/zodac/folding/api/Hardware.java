package me.zodac.folding.api;

import java.util.Objects;

public class Hardware implements Identifiable {

    private static final long serialVersionUID = 311666348626596899L;

    public static final int EMPTY_HARDWARE_ID = 0;

    private int id;
    private String hardwareName;
    private String displayName;
    private String operatingSystem;
    private double multiplier;

    public Hardware() {

    }

    private Hardware(final int id, final String hardwareName, final String displayName, final String operatingSystem, final double multiplier) {
        this.id = id;
        this.hardwareName = hardwareName;
        this.displayName = displayName;
        this.operatingSystem = operatingSystem;
        this.multiplier = multiplier;
    }

    public static Hardware create(final int id, final String hardwareName, final String displayName, final String os, final double multiplier) {
        return new Hardware(id, hardwareName, displayName, os, multiplier);
    }

    public static Hardware createWithoutId(final String hardwareName, final String displayName, final String os, final double multiplier) {
        return new Hardware(0, hardwareName, displayName, os, multiplier);
    }

    public static Hardware updateWithId(final int id, final Hardware hardware) {
        return new Hardware(id, hardware.getHardwareName(), hardware.getDisplayName(), hardware.getOperatingSystem(), hardware.getMultiplier());
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getHardwareName() {
        return hardwareName;
    }

    public void setHardwareName(final String hardwareName) {
        this.hardwareName = hardwareName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(final String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(final double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Hardware that = (Hardware) o;
        return id == that.id && Double.compare(that.multiplier, multiplier) == 0 && hardwareName.equals(that.hardwareName) && displayName.equals(that.displayName) && operatingSystem.equals(that.operatingSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hardwareName, displayName, operatingSystem, multiplier);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', hardwareName: '%s', displayName: '%s', operatingSystem: '%s', multiplier: '%s'}", this.getClass().getSimpleName(), id, hardwareName, displayName, operatingSystem, multiplier);
    }
}