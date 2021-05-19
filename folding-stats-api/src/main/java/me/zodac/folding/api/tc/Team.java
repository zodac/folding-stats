package me.zodac.folding.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * POJO defining a single {@link Team} participating in the <code>Team Competition</code>. There is a limit on the number of users each team can have, defined by the
 * {@link Category} description.
 * <p>
 * While each {@link Team} is made up of {@link User}s, we only refer to the ID of the {@link User} here, rather than the whole object itself.
 * <p>
 * {@link User}s may also be retired from a {@link Team}. These {@link User}s will still be referenced by the {@link Team} so their points are not lost, but they
 * will be removed once the monthly reset occurs. Retired {@link User}s do not count towards the {@link Category} limit.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private int captainUserId;
    private Set<Integer> userIds = new HashSet<>(0);
    private Set<Integer> retiredUserIds = new HashSet<>(0);

    /**
     * Creates a {@link Team}.
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link Team} from the DB response.
     *
     * @param teamId          the ID
     * @param teamName        the name of the team
     * @param teamDescription an optional description for the team
     * @param forumLink       a link to the {@link Team} thread on the forum
     * @param captainUserId   the {@link User} ID for the captain of this {@link Team}
     * @param userIds         the IDs of the active {@link User}s in the {@link Team}
     * @param retiredUserIds  the IDs of the retired {@link User}s in the {@link Team}
     * @return the created {@link Team}
     */
    public static Team create(final int teamId, final String teamName, final String teamDescription, final String forumLink, final int captainUserId, final Set<Integer> userIds, final Set<Integer> retiredUserIds) {
        return new Team(teamId, teamName, teamDescription, forumLink, captainUserId, userIds, retiredUserIds);
    }

    /**
     * Creates a {@link Team}.
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link User}, the {@link #EMPTY_TEAM_ID} will be used instead.
     *
     * @param teamName        the name of the team
     * @param teamDescription an optional description for the team
     * @param forumLink       a link to the {@link Team} thread on the forum
     * @param captainUserId   the {@link User} ID for the captain of this {@link Team}
     * @param userIds         the IDs of the active {@link User}s in the {@link Team}
     * @param retiredUserIds  the IDs of the retired {@link User}s in the {@link Team}
     * @return the created {@link Team}
     */
    public static Team createWithoutId(final String teamName, final String teamDescription, final String forumLink, final int captainUserId, final Set<Integer> userIds, final Set<Integer> retiredUserIds) {
        return new Team(EMPTY_TEAM_ID, teamName, teamDescription, forumLink, captainUserId, userIds, retiredUserIds);
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
        return new Team(teamId, team.teamName, team.teamDescription, team.forumLink, team.captainUserId, team.userIds, team.retiredUserIds);
    }

    /**
     * Retire a {@link User} from the {@link Team}.
     * <p>
     * Removes the {@link User} from the {@link Set} of active {@link User}s and adds them to the {@link Set} of retired {@link User}s. The {@link User} will no longer
     * collect new stats for the {@link Team}, but any existing stats for this monthly cycle will be retained.
     *
     * @param team          the {@link Team} from which to remove the {@link User}
     * @param userId        the ID of the {@link User} to retire
     * @param retiredUserId the ID of the {@link User} that has been retired (provided from the DB after {@link User#retireUser(User)}
     * @return the {@link Team} with the {@link User}
     */
    public static Team retireUser(final Team team, final int userId, final int retiredUserId) {
        final Set<Integer> updateUserIds = new HashSet<>(team.getUserIds());
        updateUserIds.remove(userId);

        final Set<Integer> retiredUserIds = new HashSet<>(team.getRetiredUserIds());
        retiredUserIds.add(retiredUserId);

        return new Team(team.id, team.teamName, team.teamDescription, team.forumLink, team.captainUserId, updateUserIds, retiredUserIds);
    }

    /**
     * Un-retire a {@link User} for the {@link Team}.
     * <p>
     * Adds the {@link User} to the {@link Set} of active {@link User}s and removed them from the {@link Set} of retired {@link User}s (if they were previously
     * retired from this {@link Team}. The {@link User} will start collecting stats for the {@link Team} again from the next update.
     *
     * @param team          the {@link Team} from which to remove the {@link User}
     * @param userId        the ID of the {@link User} to retire
     * @param retiredUserId the ID of the {@link User} that has been retired (provided from the DB after {@link User#retireUser(User)}
     * @return the {@link Team} with the {@link User}
     */
    public static Team unretireUser(final Team team, final int userId, final int retiredUserId) {
        final Set<Integer> updateUserIds = new HashSet<>(team.getUserIds());
        updateUserIds.add(userId);

        final Set<Integer> retiredUserStats = new HashSet<>(team.getRetiredUserIds());
        retiredUserStats.remove(retiredUserId);

        return new Team(team.id, team.teamName, team.teamDescription, team.forumLink, team.captainUserId, updateUserIds, retiredUserStats);
    }

    /**
     * Removes all retired {@link User}s from the {@link Team}.
     * <p>
     * Since this will remove the reference to the retired {@link User}, the points will also be removed from the {@link Team}.
     *
     * @param team the {@link Team} with the retired {@link User}s to remove
     * @return the {@link Team} with no retired {@link User}s
     */
    public static Team removeRetiredUsers(final Team team) {
        return new Team(team.id, team.teamName, team.teamDescription, team.forumLink, team.captainUserId, team.userIds, Collections.emptySet());
    }
}
