package me.zodac.folding.ejb.tc.lars;

import java.io.Serializable;
import java.util.Comparator;
import me.zodac.folding.api.tc.Hardware;

/**
 * Custom {@link Comparator} that allows for sorting of {@link Hardware}s by only comparing the value of {@link Hardware#getHardwareName()}.
 */
final class HardwareNameComparator implements Comparator<Hardware>, Serializable {

    private static final long serialVersionUID = -7988652908218460899L;

    private HardwareNameComparator() {

    }

    /**
     * Creates an instance of {@link HardwareNameComparator}.
     *
     * @return the created {@link HardwareNameComparator}
     */
    public static HardwareNameComparator create() {
        return new HardwareNameComparator();
    }

    @Override
    public int compare(final Hardware first, final Hardware second) {
        return String.CASE_INSENSITIVE_ORDER.compare(first.getHardwareName(), second.getHardwareName());
    }
}