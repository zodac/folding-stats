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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserRequest}.
 */
class UserRequestTest {

    private static final String VALID_PROFILE_LINK = "https://google.com";
    private static final String VALID_LIVE_STATS_LINK = "https://google.ie";
    private static final String DUMMY_PASSKEY = "DummyPasskey01234567890123456789";

    @Test
    void testCreate_captain() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        // Expecting no exception
        userRequest.validate();

        assertThat(userRequest)
            .extracting("foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "hardwareId", "teamId",
                "userIsCaptain")
            .containsExactly("user", "user", "DummyPasskey01234567890123456789", Category.AMD_GPU.toString(), "https://google.com",
                "https://google.ie", 1, 1, true);
    }

    @Test
    void testCreate_teamMember() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            false
        );

        // Expecting no exception
        userRequest.validate();

        assertThat(userRequest)
            .extracting("foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "hardwareId", "teamId",
                "userIsCaptain")
            .containsExactly("user", "user", "DummyPasskey01234567890123456789", Category.AMD_GPU.toString(), "https://google.com",
                "https://google.ie", 1, 1, false);
    }

    @Test
    void testCreate_foldingUserNameNull() {
        final UserRequest userRequest = new UserRequest(
            null,
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_foldingUserNameBlank() {
        final UserRequest userRequest = new UserRequest(
            "",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_foldingUserNameInvalidCharacter() {
        final UserRequest userRequest = new UserRequest(
            "*",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_displayNameNull() {
        final UserRequest userRequest = new UserRequest(
            "user",
            null,
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_displayBlank() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_passkeyNull() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            null,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_passkeyBlank() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            "",
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_passkeyInvalidLength() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            "ShortPasskey",
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_categoryInvalid() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.INVALID.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_profileLinkInvalidUrl() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            "invalidUrl",
            VALID_LIVE_STATS_LINK,
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_liveStatsLinkInvalid() {
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            "invalidUrl",
            1,
            1,
            true
        );

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }
}
