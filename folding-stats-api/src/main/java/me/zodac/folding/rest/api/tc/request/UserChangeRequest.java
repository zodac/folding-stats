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
import static me.zodac.folding.api.util.StringUtils.isBlankOrValidUrl;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.exception.ValidationException;

/**
 * REST request to create/update a {@link me.zodac.folding.api.tc.change.UserChange}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Accessors(fluent = false) // Need #get*()
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
@Schema(name = "UserChangeRequest",
    description = "An example request to create a user change request, with all fields",
    example = """
        {
          "userId": 1,
          "existingPasskey": "12345678912345678912345678912345",
          "foldingUserName": "User1",
          "passkey": "12345678912345678912345678912345",
          "liveStatsLink": "https://www.google.com",
          "hardwareId": 1,
          "immediate": true
        }"""
)
public class UserChangeRequest implements RequestPojo {

    private static final Pattern FOLDING_USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\d._-]*$");
    private static final Pattern PASSKEY_PATTERN = Pattern.compile("[a-zA-Z\\d]{32}");

    @Schema(
        description = "The ID of the user for whom a change is being requested",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    private int userId;

    @Schema(
        description = "The current passkey for the user",
        example = "12345678912345678912345678912345",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String existingPasskey;

    @Schema(
        description = "The foldingUserName that the user will use (leave as existing value if no change is required)",
        example = "User1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    private String foldingUserName;

    @Schema(
        description = "The passkey that the user will use (leave as existing value if no change is required)",
        example = "12345678912345678912345678912345",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String passkey;

    @Schema(
        description = "A link to the live stats for the user  (leave as existing value if no change is required)",
        example = "https://www.google.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    private String liveStatsLink;

    @Schema(
        description = "The ID of the hardware that the user will use (leave as existing value if no change is required)",
        example = "4",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    private int hardwareId;

    @Schema(
        description = "Whether the change should be applied immediately, or for the next month's competition",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    private boolean immediate;

    /**
     * Simple check that validates that the REST payload is valid. Checks that:
     * <ul>
     *     <li>'foldingUserName' matches {@link #FOLDING_USER_NAME_PATTERN}</li>
     *     <li>'passkey' matches {@link #PASSKEY_PATTERN}</li>
     *     <li>'liveStatsLink' is null or empty, or else is a valid {@link java.net.URI}</li>
     * </ul>
     *
     * @throws me.zodac.folding.api.exception.ValidationException thrown if there are any validation failures
     */
    public void validate() {
        final Collection<String> failureMessages = Stream.of(
                validateFoldingUserName(),
                validatePasskey(),
                validateLiveStatsLink()
            )
            .filter(Objects::nonNull)
            .toList();
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(this, failureMessages);
        }
    }

    private String validateFoldingUserName() {
        return isBlank(foldingUserName) || !FOLDING_USER_NAME_PATTERN.matcher(foldingUserName).find()
            ? "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen"
            : null;
    }

    private String validatePasskey() {
        return isBlank(passkey) || !PASSKEY_PATTERN.matcher(passkey).find()
            ? "Field 'passkey' must be 32 characters long and include only alphanumeric characters"
            : null;
    }

    private String validateLiveStatsLink() {
        return isBlankOrValidUrl(liveStatsLink)
            ? null
            : String.format("Field 'liveStatsLink' is not a valid link: '%s'", liveStatsLink);
    }
}
