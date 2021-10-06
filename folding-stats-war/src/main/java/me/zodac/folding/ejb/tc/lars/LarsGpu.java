package me.zodac.folding.ejb.tc.lars;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Representation of a GPU entry from the LARS DB.
 *
 * @see <a href="https://folding.lar.systems/gpu_ppd/overall_ranks">LARS GPU PPD database</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LarsGpu {

    private final String displayName;
    private final String manufacturer;
    private final String modelInfo;
    private final int rank;
    private final long averagePpd;

    /**
     * Create a {@link LarsGpu}.
     *
     * @param displayName  the name
     * @param manufacturer the manufacturer
     * @param modelInfo    the model information
     * @param rank         the {@link LarsGpu}'s PPD rank compared to all other {@link LarsGpu}s
     * @param averagePpd   the average PPD for all operating systems
     * @return the created {@link LarsGpu}
     */
    public static LarsGpu create(final String displayName, final String manufacturer, final String modelInfo, final int rank, final long averagePpd) {
        return new LarsGpu(displayName, manufacturer, modelInfo, rank, averagePpd);
    }
}
