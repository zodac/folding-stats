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

package me.zodac.folding.api.stats;

import static org.assertj.core.api.Assertions.assertThat;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link FoldingStatsDetails}.
 */
class FoldingStatsDetailsTest {

    @Test
    void testCreate() {
        final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.create("foldingUserName", "passkey");
        assertThat(foldingStatsDetails.foldingUserName())
            .isEqualTo("foldingUserName");
        assertThat(foldingStatsDetails.passkey())
            .isEqualTo("passkey");
    }

    @Test
    void testCreateFromUser() {
        final User user = createUser();
        final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.createFromUser(user);
        assertThat(foldingStatsDetails.foldingUserName())
            .isEqualTo("foldingUserName");
        assertThat(foldingStatsDetails.passkey())
            .isEqualTo("passkey");
    }

    private static User createUser() {
        final Hardware hardware = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "hardwareName", "displayName", HardwareMake.AMD,
            HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(Team.EMPTY_TEAM_ID, "team", "", "");
        return User.create(User.EMPTY_USER_ID, "foldingUserName", "displayName", "passkey",
            Category.AMD_GPU, "", "", hardware, team, Role.CAPTAIN);
    }
}
