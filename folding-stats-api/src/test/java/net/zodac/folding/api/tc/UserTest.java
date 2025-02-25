/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.api.tc;

import static org.assertj.core.api.Assertions.assertThat;

import net.zodac.folding.rest.api.tc.request.UserRequest;
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
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, hardware, team, Role.CAPTAIN);

        assertThat(user.id())
            .isEqualTo(1);
        assertThat(user.foldingUserName())
            .isEqualTo("user");
        assertThat(user.displayName())
            .isEqualTo("user");
        assertThat(user.passkey())
            .isEqualTo(DUMMY_PASSKEY);
        assertThat(user.category())
            .isEqualTo(Category.AMD_GPU);
        assertThat(user.profileLink())
            .isEqualTo(VALID_PROFILE_LINK);
        assertThat(user.liveStatsLink())
            .isEqualTo(VALID_LIVE_STATS_LINK);
        assertThat(user.role())
            .isEqualTo(Role.CAPTAIN);

        assertThat(user.hardware())
            .isEqualTo(hardware);

        assertThat(user.team())
            .isEqualTo(team);
    }

    @Test
    void testCreate_noId() {
        final User user =
            User.create(User.EMPTY_USER_ID, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, "", VALID_LIVE_STATS_LINK, createHardware(),
                createTeam(),
                Role.CAPTAIN);

        assertThat(user.id())
            .isEqualTo(User.EMPTY_USER_ID);
    }

    @Test
    void testCreate_profileLinkBlank() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, "", VALID_LIVE_STATS_LINK, createHardware(), createTeam(), Role.CAPTAIN);

        assertThat(user.profileLink())
            .isNull();
    }

    @Test
    void testCreate_profileLinkNull() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, null, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                Role.CAPTAIN);

        assertThat(user.profileLink())
            .isNull();
    }

    @Test
    void testCreate_liveStatsLinkBlank() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, "", createHardware(), createTeam(), Role.CAPTAIN);

        assertThat(user.liveStatsLink())
            .isNull();
    }

    @Test
    void testCreate_liveStatsLinkNull() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, null, createHardware(), createTeam(), Role.CAPTAIN);

        assertThat(user.liveStatsLink())
            .isNull();
    }

    @Test
    void testRemoveCaptaincy() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                Role.CAPTAIN);

        final User updatedUser = User.removeCaptaincyFromUser(user);
        assertThat(updatedUser.role())
            .isEqualTo(Role.MEMBER);

        final User reupdatedUser = User.removeCaptaincyFromUser(updatedUser);
        assertThat(reupdatedUser.role())
            .isEqualTo(Role.MEMBER);
    }

    @Test
    void testHidePasskey() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                Role.CAPTAIN);

        assertThat(user.passkey())
            .isEqualTo(DUMMY_PASSKEY);
        assertThat(user.isPasskeyHidden())
            .isFalse();

        final User updatedUser = User.hidePasskey(user);
        assertThat(updatedUser.passkey())
            .isEqualTo("DummyPas************************");
        assertThat(updatedUser.isPasskeyHidden())
            .isTrue();
    }

    @Test
    void testHidePasskey_lessThan8Chars() {
        final User user =
            User.create(1, "user", "user", "1234", Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                Role.CAPTAIN);

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
                Role.CAPTAIN);
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

        assertThat(user.isEqualRequest(userRequest))
            .isTrue();
    }

    @Test
    void testIsEqualRequest_invalid() {
        final User user =
            User.create(1, "user", "user", DUMMY_PASSKEY, Category.AMD_GPU, VALID_PROFILE_LINK, VALID_LIVE_STATS_LINK, createHardware(), createTeam(),
                Role.CAPTAIN);
        final UserRequest userRequest = new UserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU.toString(),
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            2,
            1,
            true
        );

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
