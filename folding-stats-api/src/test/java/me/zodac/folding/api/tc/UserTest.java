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

package me.zodac.folding.api.tc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User}.
 */
class UserTest {

    private static final String VALID_PROFILE_LINK = "https://google.com";
    private static final String VALID_LIVE_STATS_LINK = "https://google.ie";
    private static final String DUMMY_PASSKEY = "DummyPasskey01234567890123456789";

    @Test
    void testCreate() {
        final Hardware hardware = createHardware();
        final Team team = createTeam();

        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, hardware, team, true);

        assertThat(user)
            .extracting("id", "foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "userIsCaptain")
            .containsExactly(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, true);

        assertThat(user.hardware())
            .isEqualTo(hardware);

        assertThat(user.team())
            .isEqualTo(team);
    }

    @Test
    void testCreate_nullHardware() {
        final Team team = createTeam();
        assertThatThrownBy(
            () -> User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, null, team, true))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreate_nullTeam() {
        final Hardware hardware = createHardware();
        assertThatThrownBy(
            () -> User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, hardware, null, true))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreate_noId() {
        final User user =
            User.createWithoutId("user", "user", DUMMY_PASSKEY, Category.AMD_GPU, "", VALID_LIVE_STATS_LINK, createHardware(), createTeam(), true);

        assertThat(user.id())
            .isEqualTo(User.EMPTY_USER_ID);
    }

    @Test
    void testCreate_profileLinkBlank() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, "", VALID_LIVE_STATS_LINK, createHardware(), createTeam(), true);

        assertThat(user.profileLink())
            .isNull();
    }

    @Test
    void testCreate_profileLinkNull() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, null, VALID_LIVE_STATS_LINK, createHardware(), createTeam(), true);

        assertThat(user.profileLink())
            .isNull();
    }

    @Test
    void testCreate_liveStatsLinkBlank() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, "", createHardware(), createTeam(), true);

        assertThat(user.liveStatsLink())
            .isNull();
    }

    @Test
    void testCreate_liveStatsLinkNull() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, null, createHardware(), createTeam(), true);

        assertThat(user.liveStatsLink())
            .isNull();
    }

    @Test
    void testRemoveCaptaincy() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                true);

        final User updatedUser = User.removeCaptaincyFromUser(user);
        assertThat(updatedUser.userIsCaptain())
            .isFalse();

        final User reupdatedUser = User.removeCaptaincyFromUser(updatedUser);
        assertThat(reupdatedUser.userIsCaptain())
            .isFalse();
    }

    @Test
    void testHidePasskey() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                true);

        assertThat(user.passkey())
            .isEqualTo(DUMMY_PASSKEY);

        final User updatedUser = User.hidePasskey(user);
        assertThat(updatedUser.passkey())
            .isEqualTo("DummyPas************************");
    }

    @Test
    void testHidePasskey_lessThan8Chars() {
        final User user =
            User.create(1, "user", "user", "1234", Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                true);

        assertThat(user.passkey())
            .isEqualTo("1234");

        final User updatedUser = User.hidePasskey(user);
        assertThat(updatedUser.passkey())
            .isEqualTo("1234************************");
    }

    @Test
    void testIsEqualRequest_valid() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                true);
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey(DUMMY_PASSKEY)
            .category("AMD_GPU")
            .profileLink(VALID_PROFILE_LINK)
            .liveStatsLink(VALID_LIVE_STATS_LINK)
            .hardwareId(1)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThat(user.isEqualRequest(userRequest))
            .isTrue();
    }

    @Test
    void testIsEqualRequest_invalid() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                true);
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey(DUMMY_PASSKEY)
            .category("AMD_GPU")
            .profileLink(VALID_PROFILE_LINK)
            .liveStatsLink(VALID_LIVE_STATS_LINK)
            .hardwareId(2)
            .teamId(1)
            .userIsCaptain(true)
            .build();

        assertThat(user.isEqualRequest(userRequest))
            .isFalse();
    }

    private static Hardware createHardware() {
        return Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
    }

    private static Team createTeam() {
        return Team.create(1, "team", "teamDescription", "https://google.com");
    }
}
