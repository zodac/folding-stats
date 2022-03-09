/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.rest.api.tc.request;

import static me.zodac.folding.api.util.StringUtils.isBlank;
import static me.zodac.folding.api.util.StringUtils.isBlankOrValidUrl;

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
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;

/**
 * REST request to create/update a {@link me.zodac.folding.api.tc.User}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserRequest implements RequestPojo {

    /**
     * {@link Pattern} defining a valid Folding@Home username.
     */
    public static final Pattern FOLDING_USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]*$");

    /**
     * {@link Pattern} defining a valid Folding@home passkey for a user.
     */
    public static final Pattern PASSKEY_PATTERN = Pattern.compile("[a-zA-Z0-9]{32}");

    private String foldingUserName;
    private String displayName;
    private String passkey;
    private String category;
    private String profileLink;
    private String liveStatsLink;
    private int hardwareId;
    private int teamId;
    private boolean userIsCaptain;

    @Override
    public void validate() {
        final Collection<String> failureMessages = Stream.of(
                validateFoldingUserName(),
                validateDisplayName(),
                validatePasskey(),
                validateCategory(),
                validateProfileLink(),
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

    private String validateDisplayName() {
        return isBlank(displayName)
            ? "Field 'displayName' must not be empty"
            : null;
    }

    private String validatePasskey() {
        return isBlank(passkey) || !PASSKEY_PATTERN.matcher(passkey).find()
            ? "Field 'passkey' must be 32 characters long and include only alphanumeric characters"
            : null;
    }

    private String validateCategory() {
        return Category.get(category) == Category.INVALID
            ? String.format("Field 'category' must be one of: %s", Category.getAllValues())
            : null;
    }

    private String validateProfileLink() {
        return isBlankOrValidUrl(profileLink)
            ? null
            : String.format("Field 'profileLink' is not a valid link: '%s'", profileLink);
    }

    private String validateLiveStatsLink() {
        return isBlankOrValidUrl(liveStatsLink)
            ? null
            : String.format("Field 'liveStatsLink' is not a valid link: '%s'", liveStatsLink);
    }
}
