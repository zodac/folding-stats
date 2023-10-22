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

package me.zodac.folding.rest.api.tc.request;

import static me.zodac.folding.api.util.StringUtils.isBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.stream.Stream;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;

/**
 * REST request to create/update a {@link me.zodac.folding.api.tc.Hardware}.
 */
@Schema(name = "HardwareRequest",
    description = "An example request to create a hardware, with all fields",
    example = """
        {
          "hardwareName": "GA106 [GeForce RTX 3060]",
          "displayName": "GeForce RTX 3060",
          "hardwareMake": "NVIDIA",
          "hardwareType": "GPU",
          "multiplier": 10.50,
          "averagePpd": 555
        }"""
)
public record HardwareRequest(
    @Schema(
        description = "The internal, unique name of the hardware",
        example = "GA106 [GeForce RTX 3060]",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String hardwareName,
    @Schema(
        description = "The user-friendly display name of the hardware",
        example = "GeForce RTX 3060",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String displayName,
    @Schema(
        description = "The manufacturer of the hardware (case-sensitive)",
        example = "NVIDIA",
        oneOf = HardwareMake.class,
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String hardwareMake,
    @Schema(
        description = "The type of the hardware (case-sensitive)",
        example = "GPU",
        oneOf = HardwareType.class,
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String hardwareType,
    @Schema(
        description = "The multiplier of the hardware, compared to the best performing hardware of the same type",
        example = "10.50",
        minimum = "1.00",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    double multiplier,
    @Schema(
        description = "The average PPD of the hardware",
        example = "555",
        minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    long averagePpd
) implements RequestPojo {

    // The hardware with the highest PPD will have a multiplier of <b>1.00</b>, and all others will be based on that
    private static final double MINIMUM_MULTIPLIER_VALUE = 1.00D;
    private static final long MINIMUM_AVERAGE_PPD_VALUE = 1L;

    /**
     * Simple check that validates that the REST payload is valid. Checks that:
     * <ul>
     *     <li>'hardwareName' is not null or empty</li>
     *     <li>'displayName' is not null or empty</li>
     *     <li>'hardwareMake' is a valid {@link HardwareMake}</li>
     *     <li>'hardwareType' is a valid {@link HardwareType}</li>
     *     <li>'multiplier' is at least <b>{@value #MINIMUM_MULTIPLIER_VALUE}</b></li>
     *     <li>'averagePpd' is at least <b>{@value #MINIMUM_AVERAGE_PPD_VALUE}</b></li>
     * </ul>
     *
     * @throws me.zodac.folding.api.exception.ValidationException thrown if there are any validation failures
     */
    public void validate() {
        final Collection<String> failureMessages = Stream.of(
                validateHardwareName(),
                validateDisplayName(),
                validateHardwareMake(),
                validateHardwareType(),
                validateMultiplier(),
                validateAveragePpd()
            )
            .filter(s -> !s.isEmpty())
            .toList();
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(this, failureMessages);
        }
    }

    private String validateHardwareName() {
        return isBlank(hardwareName)
            ? "Field 'hardwareName' must not be empty"
            : "";
    }

    private String validateDisplayName() {
        return isBlank(displayName)
            ? "Field 'displayName' must not be empty"
            : "";
    }

    private String validateHardwareMake() {
        return HardwareMake.get(hardwareMake) == HardwareMake.INVALID
            ? String.format("Field 'hardwareMake' must be one of: %s", HardwareMake.getAllValues())
            : "";
    }

    private String validateHardwareType() {
        return HardwareType.get(hardwareType) == HardwareType.INVALID
            ? String.format("Field 'hardwareType' must be one of: %s", HardwareType.getAllValues())
            : "";
    }

    private String validateMultiplier() {
        return multiplier >= MINIMUM_MULTIPLIER_VALUE
            ? ""
            : String.format("Field 'multiplier' must be %.2f or higher", MINIMUM_MULTIPLIER_VALUE);
    }

    private String validateAveragePpd() {
        return averagePpd >= MINIMUM_AVERAGE_PPD_VALUE
            ? ""
            : String.format("Field 'averagePpd' must be %d or higher", MINIMUM_AVERAGE_PPD_VALUE);
    }
}
