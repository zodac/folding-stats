/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.api.tc;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;

/**
 * POJO defining a piece of {@link Hardware} for use in the {@code Team Competition}.
 *
 * <p>
 * Each {@link Hardware} will have a multiplier which is calculated from the LARS PPD database, where the best piece of {@link Hardware} has a
 * multiplier of <b>1.00</b>, and each other piece of {@link Hardware}'s multiplier is:
 * <pre>
 *     Best PPD / PPD for given {@link Hardware} (to 2 decimal places)
 * </pre>
 *
 * @see <a href="https://https://folding.lar.systems/">LARS PPD database</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public final class Hardware implements ResponsePojo {

    /**
     * The default {@link Hardware} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_HARDWARE_ID = 0;

    private final int id;
    private final String hardwareName;
    private final String displayName;
    private final HardwareMake hardwareMake;
    private final HardwareType hardwareType;
    private final double multiplier;
    private final long averagePpd;

    /**
     * Creates a {@link Hardware}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link Hardware} from the DB response.
     *
     * @param id           the ID
     * @param hardwareName the LARS DB name
     * @param displayName  the display name for the {@code Team Competition}
     * @param hardwareMake the {@link HardwareMake} of the {@link Hardware}
     * @param hardwareType the {@link HardwareType} of the {@link Hardware}
     * @param multiplier   the calculated multiplier
     * @param averagePpd   the average PPD of the {@link Hardware}
     * @return the created {@link Hardware}
     */
    public static Hardware create(final int id,
                                  final String hardwareName,
                                  final String displayName,
                                  final HardwareMake hardwareMake,
                                  final HardwareType hardwareType,
                                  final double multiplier,
                                  final long averagePpd) {
        final String unescapedHardwareName = StringUtils.unescapeHtml(hardwareName);
        return new Hardware(id, unescapedHardwareName, displayName, hardwareMake, hardwareType, multiplier, averagePpd);
    }

    /**
     * Creates a {@link Hardware}.
     *
     * <p>
     * We assume the provided {@link HardwareRequest}'s {@link HardwareMake} and {@link HardwareType} have already been validated.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link Hardware}, the {@link #EMPTY_HARDWARE_ID} will be used instead.
     *
     * @param hardwareRequest the input {@link HardwareRequest} from the REST endpoint
     * @return the created {@link Hardware}
     */
    public static Hardware createWithoutId(final HardwareRequest hardwareRequest) {
        return create(
            EMPTY_HARDWARE_ID,
            hardwareRequest.hardwareName(),
            hardwareRequest.displayName(),
            HardwareMake.get(hardwareRequest.hardwareMake()),
            HardwareType.get(hardwareRequest.hardwareType()),
            hardwareRequest.multiplier(),
            hardwareRequest.averagePpd()
        );
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
        return create(
            hardwareId,
            hardware.hardwareName,
            hardware.displayName,
            hardware.hardwareMake,
            hardware.hardwareType,
            hardware.multiplier,
            hardware.averagePpd
        );
    }

    /**
     * Checks if the input {@link HardwareRequest} is equal to the {@link Hardware}.
     *
     * <p>
     * While the {@link HardwareRequest} will likely not be a complete match, there should be enough fields to verify
     * if it is the same as an existing {@link Hardware}.
     *
     * @param hardwareRequest input {@link HardwareRequest}
     * @return {@code true} if the input{@link HardwareRequest} is equal to the {@link Hardware}
     */
    public boolean isEqualRequest(final HardwareRequest hardwareRequest) {
        return Double.compare(multiplier, hardwareRequest.multiplier()) == 0
            && Objects.equals(hardwareName, hardwareRequest.hardwareName())
            && Objects.equals(displayName, hardwareRequest.displayName())
            && Objects.equals(hardwareMake.toString(), hardwareRequest.hardwareMake())
            && Objects.equals(hardwareType.toString(), hardwareRequest.hardwareType())
            && averagePpd == hardwareRequest.averagePpd();
    }
}
