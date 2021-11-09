/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.rest.api.tc.request.TeamRequest;

/**
 * POJO defining a single {@link Team} participating in the <code>Team Competition</code>. There is a limit on the number of users each team can have,
 * defined by the {@link Category}.
 *
 * <p>
 * While each {@link Team} is made up of {@link User}s we do not keep any reference to the {@link User} in this object.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Team implements ResponsePojo {

    /**
     * The default {@link Team} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_TEAM_ID = 0;

    private final int id;
    private final String teamName;
    private final String teamDescription;
    private final String forumLink;

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
    public static Team create(final int teamId, final String teamName, final String teamDescription, final String forumLink) {
        final String teamDescriptionOrNull = isEmpty(teamDescription) ? null : teamDescription;
        final String forumLinkOrNull = isEmpty(forumLink) ? null : forumLink;
        return new Team(teamId, teamName, teamDescriptionOrNull, forumLinkOrNull);
    }

    /**
     * Creates a {@link Team}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link Team}, the {@link #EMPTY_TEAM_ID} will be used instead.
     *
     * @param teamName        the name of the team
     * @param teamDescription an optional description for the team
     * @param forumLink       a link to the {@link Team} thread on the forum
     * @return the created {@link Team}
     */
    public static Team createWithoutId(final String teamName, final String teamDescription, final String forumLink) {
        return create(EMPTY_TEAM_ID, teamName, teamDescription, forumLink);
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
        return createWithoutId(teamRequest.getTeamName(), teamRequest.getTeamDescription(), teamRequest.getForumLink());
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

    private static boolean isEmpty(final String input) {
        return input == null || input.isBlank();
    }

    /**
     * Checks if the input {@link TeamRequest} is equal to the {@link Team}.
     *
     * <p>
     * While the {@link TeamRequest} will likely not be a complete match, there should be enough fields to verify
     * if it is the same as an existing {@link Team}.
     *
     * @param teamRequest input {@link TeamRequest}
     * @return <code>true</code> if the input{@link TeamRequest} is equal to the {@link Team}
     */
    public boolean isEqualRequest(final TeamRequest teamRequest) {
        return Objects.equals(teamName, teamRequest.getTeamName())
            && Objects.equals(teamDescription, teamRequest.getTeamDescription())
            && Objects.equals(forumLink, teamRequest.getForumLink());
    }
}
