package me.zodac.folding.api.rest;

import java.io.Serializable;
import java.util.Objects;

public class HardwareCategory implements Serializable {

    private static final long serialVersionUID = 311666348626596899L;

    private String categoryName;
    private double multiplier;

    public HardwareCategory() {

    }

    public HardwareCategory(final String categoryName, final double multiplier) {
        this.categoryName = categoryName;
        this.multiplier = multiplier;
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

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final HardwareCategory that = (HardwareCategory) o;
        return categoryName.equals(that.categoryName)
                && Double.compare(that.multiplier, multiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, multiplier);
    }

    @Override
    public String toString() {
        return String.format("%s::{categoryName: '%s', multiplier: '%s'", this.getClass().getSimpleName(), categoryName, multiplier);
    }
}
