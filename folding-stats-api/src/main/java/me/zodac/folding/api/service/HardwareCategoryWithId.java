package me.zodac.folding.api.service;

import me.zodac.folding.api.rest.HardwareCategory;

import java.io.Serializable;
import java.util.Objects;

// TODO: [zodac] Using this since the REST layer for the POST doesn't use the ID
//   Can probably combine this with HardwareCategory, but I'll worry about that later
public class HardwareCategoryWithId implements Serializable {

    private static final long serialVersionUID = 1571008277143810317L;

    private final int id;
    private final String categoryName;
    private final double multiplier;

    private HardwareCategoryWithId(final int id, final String categoryName, final double multiplier) {
        this.id = id;
        this.categoryName = categoryName;
        this.multiplier = multiplier;
    }

    public static HardwareCategoryWithId updateHardwareCategoryWithId(final int id, final HardwareCategory hardwareCategory) {
        return new HardwareCategoryWithId(id, hardwareCategory.getCategoryName(), hardwareCategory.getMultiplier());
    }

    public static HardwareCategoryWithId createHardwareCategoryWithId(final int id, final String categoryName, final double multiplier) {
        return new HardwareCategoryWithId(id, categoryName, multiplier);
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final HardwareCategoryWithId that = (HardwareCategoryWithId) o;
        return id == that.id && Double.compare(that.multiplier, multiplier) == 0 && categoryName.equals(that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryName, multiplier);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', categoryName: '%s', multiplier: '%s'", this.getClass().getSimpleName(), id, categoryName, multiplier);
    }
}
