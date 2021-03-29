package me.zodac.folding.api;

import java.io.Serializable;
import java.util.Objects;

import static me.zodac.folding.api.util.StringUtils.isNotBlank;

public class HardwareCategory implements Serializable {

    private static final long serialVersionUID = 311666348626596899L;

    private int id;
    private String categoryName;
    private double multiplier;

    public HardwareCategory() {

    }

    private HardwareCategory(final int id, final String categoryName, final double multiplier) {
        this.id = id;
        this.categoryName = categoryName;
        this.multiplier = multiplier;
    }

    public static HardwareCategory create(final int id, final String categoryName, final double multiplier) {
        return new HardwareCategory(id, categoryName, multiplier);
    }

    public static HardwareCategory updateHardwareCategoryWithId(final int id, final HardwareCategory hardwareCategory) {
        return new HardwareCategory(id, hardwareCategory.getCategoryName(), hardwareCategory.getMultiplier());
    }

    // Quick function used for REST requests. Since a JSON payload may have a missing/incorrect field, we need to check
    // I am assuming multiplier cannot be less than 0, also assuming we might want a 0.1/0.5 at some point with future hardware
    public boolean isValid() {
        return isNotBlank(categoryName) && multiplier > 0.0D;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(final String categoryName) {
        this.categoryName = categoryName;
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
        final HardwareCategory that = (HardwareCategory) o;
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
