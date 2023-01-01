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
        final Team team = Team.create(Team.EMPTY_TEAM_ID, "teamName", "teamDescription", VALID_FORUM_LINK);

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
