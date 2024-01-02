/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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
import java.util.regex.Pattern;
import java.util.stream.Stream;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.HardwareMake;
import org.checkerframework.nullaway.checker.nullness.qual.Nullable;

/**
 * REST request to create/update a {@link me.zodac.folding.api.tc.User}.
 */
@Schema(name = "UserRequest",
    description = "An example request to create a user, with all fields",
    example = """
        {
          "foldingUserName": "User1",
          "displayName": "FirstUser",
          "passkey": "fc7d6837269d86784d8bfd0b386d6bca",
          "category": "WILDCARD",
          "profileLink": "https://extremehw.net/",
          "liveStatsLink": "https://etf.axihub.ca/",
          "hardwareId": 1,
          "teamId": 1,
          "userIsCaptain": true
        }"""
)
public record UserRequest(
    @Schema(
        description = "The Folding@Home userName that the user will use for stats from the Stanford stats website",
        example = "User1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String foldingUserName,
    @Schema(
        description = "The user-friendly display name of the user",
        example = "FirstUser",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String displayName,
    @Schema(
        description = "The Folding@Home passkey of the user",
        example = "fc7d6837269d86784d8bfd0b386d6bca",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    String passkey,
    @Schema(
        description = "The category the user will compete in (case-sensitive)",
        example = "WILDCARD",
        oneOf = Category.class,
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String category,
    @Schema(
        description = "A link to the user's profile on the forum",
        example = "https://extremehw.net",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Nullable
    String profileLink,
    @Schema(
        description = "A link to the live stats for the user",
        example = "https://etf.axihub.ca",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Nullable
    String liveStatsLink,
    @Schema(
        description = "The ID of the hardware that the user will use",
        example = "4",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    int hardwareId,
    @Schema(
        description = "The ID of the team that the user will be on",
        example = "17",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    int teamId,
    @Schema(
        description = "Whether the user is captain of their team",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    boolean userIsCaptain
) implements RequestPojo {

    private static final Pattern FOLDING_USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\d._-]*$");
    private static final Pattern PASSKEY_PATTERN = Pattern.compile("[a-zA-Z\\d]{32}");

    /**
     * Simple check that validates that the REST payload is valid. Checks that:
     * <ul>
     *     <li>'foldingUserName' matches {@link #FOLDING_USER_NAME_PATTERN}</li>
     *     <li>'displayName' is not null or empty</li>
     *     <li>'passkey' matches {@link #PASSKEY_PATTERN}</li>
     *     <li>'hardwareMake' is a valid {@link HardwareMake}</li>
     *     <li>'category' is a valid {@link Category}</li>
     *     <li>'profileLink' is null or empty, or else is a valid {@link java.net.URI}</li>
     *     <li>'liveStatsLink' is null or empty, or else is a valid {@link java.net.URI}</li>
     * </ul>
     *
     * @throws me.zodac.folding.api.exception.ValidationException thrown if there are any validation failures
     */
    public void validate() {
        final Collection<String> failureMessages = Stream.of(
                validateFoldingUserName(),
                validateDisplayName(),
                validatePasskey(),
                validateCategory(),
                validateProfileLink(),
                validateLiveStatsLink()
            )
            .filter(s -> !s.isEmpty())
            .toList();
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(this, failureMessages);
        }
    }

    private String validateFoldingUserName() {
        return isBlank(foldingUserName) || !FOLDING_USER_NAME_PATTERN.matcher(foldingUserName).find()
            ? "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen"
            : "";
    }

    private String validateDisplayName() {
        return isBlank(displayName)
            ? "Field 'displayName' must not be empty"
            : "";
    }

    private String validatePasskey() {
        return isBlank(passkey) || !PASSKEY_PATTERN.matcher(passkey).find()
            ? "Field 'passkey' must be 32 characters long and include only alphanumeric characters"
            : "";
    }

    private String validateCategory() {
        return Category.get(category) == Category.INVALID
            ? String.format("Field 'category' must be one of: %s", Category.getAllValues())
            : "";
    }

    private String validateProfileLink() {
        return isBlankOrValidUrl(profileLink)
            ? ""
            : String.format("Field 'profileLink' is not a valid link: '%s'", profileLink);
    }

    private String validateLiveStatsLink() {
        return isBlankOrValidUrl(liveStatsLink)
            ? ""
            : String.format("Field 'liveStatsLink' is not a valid link: '%s'", liveStatsLink);
    }
}
