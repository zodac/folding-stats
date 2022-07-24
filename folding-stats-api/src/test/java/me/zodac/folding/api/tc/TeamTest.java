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

import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Team}.
 */
class TeamTest {

    private static final String VALID_FORUM_LINK = "https://google.com";

    @Test
    void testCreate() {
        final Team team = Team.create(1, "teamName", "teamDescription", VALID_FORUM_LINK);

        assertThat(team)
            .extracting("id", "teamName", "teamDescription", "forumLink")
            .containsExactly(1, "teamName", "teamDescription", VALID_FORUM_LINK);
    }

    @Test
    void testCreate_noId() {
        final Team team = Team.createWithoutId("teamName", "teamDescription", VALID_FORUM_LINK);

        assertThat(team.id())
            .isEqualTo(Team.EMPTY_TEAM_ID);
    }

    @Test
    void testCreate_teamDescriptionBlank() {
        final Team team = Team.create(1, "teamName", "", VALID_FORUM_LINK);

        assertThat(team.teamDescription())
            .isNull();
    }

    @Test
    void testCreate_teamDescriptionNull() {
        final Team team = Team.create(1, "teamName", null, VALID_FORUM_LINK);

        assertThat(team.teamDescription())
            .isNull();
    }

    @Test
    void testCreate_forumLinkBlank() {
        final Team team = Team.create(1, "teamName", "teamDescription", "");

        assertThat(team.forumLink())
            .isNull();
    }

    @Test
    void testCreate_forumLinkNull() {
        final Team team = Team.create(1, "teamName", "teamDescription", null);

        assertThat(team.forumLink())
            .isNull();
    }

    @Test
    void testIsEqualRequest_valid() {
        final Team team = Team.create(1, "teamName", "teamDescription", VALID_FORUM_LINK);
        final TeamRequest teamRequest = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink(VALID_FORUM_LINK)
            .build();

        assertThat(team.isEqualRequest(teamRequest))
            .isTrue();
    }

    @Test
    void testIsEqualRequest_invalid() {
        final Team team = Team.create(1, "teamName", "teamDescription", VALID_FORUM_LINK);
        final TeamRequest teamRequest = TeamRequest.builder()
            .teamName("teamName2")
            .teamDescription("teamDescription")
            .forumLink(VALID_FORUM_LINK)
            .build();

        assertThat(team.isEqualRequest(teamRequest))
            .isFalse();
    }
}
