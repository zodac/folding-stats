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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserRequest}.
 */
class UserRequestTest {

    @Test
    void testCreate_captain() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

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
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(false)
            .build();

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
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName(null)
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_foldingUserNameBlank() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_foldingUserNameInvalidCharacter() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("*")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_displayNameNull() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName(null)
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_displayBlank() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_passkeyNull() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey(null)
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_passkeyBlank() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_passkeyInvalidLength() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("ShortPasskey")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_categoryInvalid() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.INVALID.toString())
            .profileLink("https://google.com")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_profileLinkInvalidUrl() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("invalidUrl")
            .liveStatsLink("https://google.ie")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void testCreate_liveStatsLinkInvalid() {
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey01234567890123456789")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://google.com")
            .liveStatsLink("invalidUrl")
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThatThrownBy(userRequest::validate)
            .isInstanceOf(ValidationException.class);
    }
}
