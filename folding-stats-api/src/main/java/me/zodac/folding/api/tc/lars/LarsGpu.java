package me.zodac.folding.api.tc.lars;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Representation of a GPU entry from the LARS DB.
 *
 * @see <a href="https://folding.lar.systems/gpu_ppd/overall_ranks">LARS GPU PPD database</a>
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LarsGpu {

    private String displayName;
    private String manufacturer;
    private String modelInfo;
    private int rank;
    private long averagePpd;

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
