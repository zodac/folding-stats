package me.zodac.folding.lars;

import java.io.Serializable;
import java.util.Comparator;
import me.zodac.folding.api.tc.lars.LarsGpu;

/**
 * Custom {@link Comparator} that allows for sorting of {@link LarsGpu}s by only comparing the value of {@link LarsGpu#getAveragePpd()}.
 */
final class LarsGpuAveragePpdComparator implements Comparator<LarsGpu>, Serializable {

    private static final long serialVersionUID = -7988652908218460899L;

    private LarsGpuAveragePpdComparator() {

    }

    /**
     * Creates an instance of {@link LarsGpuAveragePpdComparator}.
     *
     * @return the created {@link LarsGpuAveragePpdComparator}
     */
    static LarsGpuAveragePpdComparator create() {
        return new LarsGpuAveragePpdComparator();
    }

    @Override
    public int compare(final LarsGpu first, final LarsGpu second) {
        return Long.compare(second.getAveragePpd(), first.getAveragePpd());
    }
}