package me.zodac.folding.api.tc;

import me.zodac.folding.api.Identifiable;

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
        this.hardwareName = hardwareName == null ? null : hardwareName.trim();
        this.displayName = displayName == null ? null : displayName.trim();
        this.operatingSystem = operatingSystem == null ? null : operatingSystem.trim();
        this.multiplier = multiplier;
    }

    public static Hardware create(final int id, final String hardwareName, final String displayName, final String os, final double multiplier) {
        return new Hardware(id, hardwareName, displayName, os, multiplier);
    }

    public static Hardware updateWithId(final int id, final Hardware hardware) {
        return new Hardware(id, hardware.hardwareName, hardware.displayName, hardware.operatingSystem, hardware.multiplier);
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
        this.hardwareName = hardwareName == null ? null : hardwareName.trim();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName == null ? null : displayName.trim();
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(final String operatingSystem) {
        this.operatingSystem = operatingSystem == null ? null : operatingSystem.trim();
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
        final Hardware hardware = (Hardware) o;
        return id == hardware.id && Double.compare(hardware.multiplier, multiplier) == 0 && Objects.equals(hardwareName, hardware.hardwareName) && Objects.equals(displayName, hardware.displayName) && Objects.equals(operatingSystem, hardware.operatingSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hardwareName, displayName, operatingSystem, multiplier);
    }


    @Override
    public String toString() {
        return "Hardware::{" +
                "id: " + id +
                ", hardwareName: '" + hardwareName + "'" +
                ", displayName: '" + displayName + "'" +
                ", operatingSystem: '" + operatingSystem + "'" +
                ", multiplier: " + multiplier +
                '}';
    }
}
