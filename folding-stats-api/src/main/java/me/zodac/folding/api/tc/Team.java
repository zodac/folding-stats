package me.zodac.folding.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;

/**
 * POJO defining a single {@link Team} participating in the <code>Team Competition</code>. There is a limit on the number of users each team can have, defined by the
 * {@link Category} description.
 * <p>
 * While each {@link Team} is made up of {@link User}s we do not keep any reference to the {@link User} in this object.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Team implements Identifiable {

    /**
     * The default {@link Team} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_TEAM_ID = 0;

    private int id;
    private String teamName;
    private String teamDescription;
    private String forumLink;

    /**
     * Creates a {@link Team}.
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
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link Team}, the {@link #EMPTY_TEAM_ID} will be used instead.
     *
     * @param teamName        the name of the team
     * @param teamDescription an optional description for the team
     * @param forumLink       a link to the {@link Team} thread on the forum
     * @return the created {@link Team}
     */
    public static Team createWithoutId(final String teamName, final String teamDescription, final String forumLink) {
        final String teamDescriptionOrNull = isEmpty(teamDescription) ? null : teamDescription;
        final String forumLinkOrNull = isEmpty(forumLink) ? null : forumLink;
        return new Team(EMPTY_TEAM_ID, teamName, teamDescriptionOrNull, forumLinkOrNull);
    }

    /**
     * Updates a {@link Team} with the given ID.
     * <p>
     * Once the {@link Team} has been persisted in the DB, we will know its ID. We create a new {@link Team} instance with this ID,
     * which can be used to retrieval/referencing later.
     *
     * @param teamId the DB-generated ID
     * @param team   the {@link Team} to be updated with the ID
     * @return the updated {@link Team}
     */
    public static Team updateWithId(final int teamId, final Team team) {
        final String teamDescription = isEmpty(team.teamDescription) ? null : team.teamDescription;
        final String forumLink = isEmpty(team.forumLink) ? null : team.forumLink;
        return new Team(teamId, team.teamName, teamDescription, forumLink);
    }

    private static boolean isEmpty(final String input) {
        return input == null || input.isBlank();
    }
}
