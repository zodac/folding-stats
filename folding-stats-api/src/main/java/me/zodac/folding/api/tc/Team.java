/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.jspecify.annotations.Nullable;

/**
 * POJO defining a single {@link Team} participating in the {@code Team Competition}. There is a limit on the number of users each team can have,
 * defined by the {@link Category}.
 *
 * <p>
 * While each {@link Team} is made up of {@link User}s we do not keep any reference to the {@link User} in this class.
 */
@Getter
@Accessors(fluent = true)
@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public final class Team implements ResponsePojo {

    /**
     * The default {@link Team} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_TEAM_ID = 0;

    private final int id;
    private final String teamName;
    private final @Nullable String teamDescription;
    private final @Nullable String forumLink;

    /**
     * Constructor.
     *
     * @param id              the ID
     * @param teamName        the name of the team
     * @param teamDescription an optional description for the team
     * @param forumLink       a link to the {@link Team} thread on the forum
     */
    public Team(final int id, final String teamName, @Nullable final String teamDescription, @Nullable final String forumLink) {
        this.id = id;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.forumLink = forumLink;
    }

    /**
     * Creates a {@link Team}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link Team} from the DB response.
     *
     * @param teamId          the ID
     * @param teamName        the name of the team
     * @param teamDescription an optional description for the team
     * @param forumLink       a link to the {@link Team} thread on the forum
     * @return the created {@link Team}
     */
    public static Team create(final int teamId, final String teamName, final @Nullable String teamDescription, final @Nullable String forumLink) {
        final String unescapedTeamName = StringUtils.unescapeHtml(teamName);
        final String teamDescriptionOrNull = StringUtils.isBlank(teamDescription) ? null : teamDescription;
        final String forumLinkOrNull = StringUtils.isBlank(forumLink) ? null : forumLink;

        return new Team(teamId, unescapedTeamName, teamDescriptionOrNull, forumLinkOrNull);
    }

    /**
     * Creates a {@link Team}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link Team}, the {@link #EMPTY_TEAM_ID} will be used instead.
     *
     * @param teamRequest the input {@link TeamRequest} from the REST endpoint
     * @return the created {@link Team}
     */
    public static Team createWithoutId(final TeamRequest teamRequest) {
        return create(EMPTY_TEAM_ID, teamRequest.teamName(), teamRequest.teamDescription(), teamRequest.forumLink());
    }

    /**
     * Updates a {@link Team} with the given ID.
     *
     * <p>
     * Once the {@link Team} has been persisted in the DB, we will know its ID. We create a new {@link Team} instance with this ID,
     * which can be used to retrieval/referencing later.
     *
     * @param teamId the DB-generated ID
     * @param team   the {@link Team} to be updated with the ID
     * @return the updated {@link Team}
     */
    public static Team updateWithId(final int teamId, final Team team) {
        return create(teamId, team.teamName, team.teamDescription, team.forumLink);
    }

    /**
     * Checks if the input {@link TeamRequest} is equal to the {@link Team}.
     *
     * <p>
     * While the {@link TeamRequest} will likely not be a complete match, there should be enough fields to verify
     * if it is the same as an existing {@link Team}.
     *
     * @param teamRequest input {@link TeamRequest}
     * @return {@code true} if the input{@link TeamRequest} is equal to the {@link Team}
     */
    public boolean isEqualRequest(final TeamRequest teamRequest) {
        return Objects.equals(teamName, teamRequest.teamName())
            && Objects.equals(teamDescription, teamRequest.teamDescription())
            && Objects.equals(forumLink, teamRequest.forumLink());
    }
}
