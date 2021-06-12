package me.zodac.folding.api.tc;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;


/**
 * POJO defining a piece of {@link Hardware} for use in the <code>Team Competition</code>.
 *
 * <p>
 * Each {@link Hardware} will have a multiplier which is calculated from the LARS PPD database, where the best piece of {@link Hardware} has a
 * multiplier of <b>1.0</b>, and each other piece of {@link Hardware}'s multiplier is:
 * <pre>
 *     Best PPD / PPD for given {@link Hardware} on specified {@link OperatingSystem} (to 2 decimal places)
 * </pre>
 *
 * @see <a href="https://https://folding.lar.systems/">LARS PPD database</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Hardware implements ResponsePojo {

    /**
     * The default {@link Hardware} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_HARDWARE_ID = 0;

    private final int id;
    private final String hardwareName;
    private final String displayName;
    private final OperatingSystem operatingSystem;
    private final double multiplier;

    /**
     * Creates a {@link Hardware}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link Hardware} from the DB response.
     *
     * @param hardwareId      the ID
     * @param hardwareName    the LARS DB name
     * @param displayName     the display name for the <code>Team Competition</code>
     * @param operatingSystem the {@link OperatingSystem} the {@link Hardware} is running on
     * @param multiplier      the calculated multiplier
     * @return the created {@link Hardware}
     */
    public static Hardware create(final int hardwareId, final String hardwareName, final String displayName, final OperatingSystem operatingSystem,
                                  final double multiplier) {
        return new Hardware(hardwareId, hardwareName, displayName, operatingSystem, multiplier);
    }

    /**
     * Creates a {@link Hardware}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link Hardware}, the {@link #EMPTY_HARDWARE_ID} will be used instead.
     *
     * @param hardwareName    the LARS DB name
     * @param displayName     the display name for the <code>Team Competition</code>
     * @param operatingSystem the {@link OperatingSystem} the {@link Hardware} is running on
     * @param multiplier      the calculated multiplier
     * @return the created {@link Hardware}
     */
    public static Hardware createWithoutId(final String hardwareName, final String displayName, final OperatingSystem operatingSystem,
                                           final double multiplier) {
        return new Hardware(EMPTY_HARDWARE_ID, hardwareName, displayName, operatingSystem, multiplier);
    }

    /**
     * Updates a {@link Hardware} with the given ID.
     *
     * <p>
     * Once the {@link Hardware} has been persisted in the DB, we will know its ID. We create a new {@link Hardware} instance with this ID,
     * which can be used to retrieval/referencing later.
     *
     * @param hardwareId the DB-generated ID
     * @param hardware   the {@link Hardware} to be updated with the ID
     * @return the updated {@link Hardware}
     */
    public static Hardware updateWithId(final int hardwareId, final Hardware hardware) {
        return new Hardware(hardwareId, hardware.hardwareName, hardware.displayName, hardware.operatingSystem, hardware.multiplier);
    }

    @Override
    public boolean isEqualRequest(final RequestPojo inputRequest) {
        if (!(inputRequest instanceof HardwareRequest)) {
            return false;
        }

        final HardwareRequest hardwareRequest = (HardwareRequest) inputRequest;

        return Double.compare(multiplier, hardwareRequest.getMultiplier()) == 0
            && Objects.equals(hardwareName, hardwareRequest.getHardwareName())
            && Objects.equals(displayName, hardwareRequest.getDisplayName())
            && Objects.equals(operatingSystem.toString(), hardwareRequest.getOperatingSystem());
    }
}
