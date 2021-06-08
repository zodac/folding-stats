package me.zodac.folding.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.rest.api.tc.request.UserRequest;

import java.util.Objects;


/**
 * POJO defining a single {@link User} Folding on a username/passkey combination to participate in the
 * <code>Team Competition</code>.
 * <p>
 * Ideally this username/passkey will only be used on a single piece of {@link Hardware}. Though we cannot verify
 * that externally, we only allow each {@link User} a single {@link Hardware} at a time.
 * <p>
 * There is a limit on the number of users each team can have, defined by the {@link Category} description.
 * <p>
 * Each {@link User} can join a {@link Team} in order to have their Folding@Home stats retrieved, and they can
 * contribute to the <code>Team Competition</code>.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class User implements ResponsePojo {

    /**
     * The default {@link User} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_USER_ID = 0;

    private static final int PASSKEY_LENGTH_NOT_TO_HIDE = 8;
    private static final String PASSKEY_MASK = "************************"; // 24 characters

    private final int id;
    private final String foldingUserName;
    private final String displayName;
    private final String passkey;
    private final Category category;
    private final String profileLink;
    private final String liveStatsLink;
    private final Hardware hardware;
    private final Team team;
    private final boolean userIsCaptain;

    /**
     * Creates a {@link User}.
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link User} from the DB response.
     *
     * @param userId          the ID
     * @param foldingUserName the Folding@Home user name
     * @param displayName     the display name for the <code>Team Competition</code>
     * @param passkey         the Folding@Home passkey for this user
     * @param category        the {@link Category} the user is eligible for when added to a {@link Team}
     * @param profileLink     a URL linking to the {@link User}'s profile on their forum
     * @param liveStatsLink   a URL linking to the live Folding@Home stats (HFM, for example) for the {@link User}
     * @param hardware        the {@link Hardware} that this {@link User} is Folding on
     * @param team            the {@link Team} that the {@link User} is Folding for
     * @param isCaptain       whether the {@link User} is the captain of their {@link Team}
     * @return the created {@link User}
     */
    public static User create(final int userId, final String foldingUserName, final String displayName, final String passkey, final Category category, final String profileLink, final String liveStatsLink, final Hardware hardware, final Team team, final boolean isCaptain) {
        final String profileLinkOrNull = isEmpty(profileLink) ? null : profileLink;
        final String liveStatsLinkOrNull = isEmpty(liveStatsLink) ? null : liveStatsLink;
        return new User(userId, foldingUserName, displayName, passkey, category, profileLinkOrNull, liveStatsLinkOrNull, hardware, team, isCaptain);
    }

    /**
     * Creates a {@link User}.
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link User}, the {@link #EMPTY_USER_ID} will be used instead.
     *
     * @param foldingUserName the Folding@Home user name
     * @param displayName     the display name for the <code>Team Competition</code>
     * @param passkey         the Folding@Home passkey for this user
     * @param category        the {@link Category} the user is eligible for when added to a {@link Team}
     * @param profileLink     a URL linking to the {@link User}'s profile on their forum
     * @param liveStatsLink   a URL linking to the live Folding@Home stats (HFM, for example) for the {@link User}
     * @param hardware        the {@link Hardware} that this {@link User} is Folding on
     * @param team            the {@link Team} that the {@link User} is Folding for
     * @param isCaptain       whether the {@link User} is the captain of their {@link Team}
     * @return the created {@link User}
     */
    public static User createWithoutId(final String foldingUserName, final String displayName, final String passkey, final Category category, final String profileLink, final String liveStatsLink, final Hardware hardware, final Team team, final boolean isCaptain) {
        final String profileLinkOrNull = isEmpty(profileLink) ? null : profileLink;
        final String liveStatsLinkOrNull = isEmpty(liveStatsLink) ? null : liveStatsLink;
        return new User(EMPTY_USER_ID, foldingUserName, displayName, passkey, category, profileLinkOrNull, liveStatsLinkOrNull, hardware, team, isCaptain);
    }

    /**
     * Updates a {@link User} with the given ID.
     * <p>
     * Once the {@link User} has been persisted in the DB, we will know its ID. We create a new {@link User} instance with this ID,
     * which can be used to retrieval/referencing later.
     *
     * @param userId the DB-generated ID
     * @param user   the {@link User} to be updated with the ID
     * @return the updated {@link User}
     */
    public static User updateWithId(final int userId, final User user) {
        final String profileLink = isEmpty(user.profileLink) ? null : user.profileLink;
        final String liveStatsLink = isEmpty(user.liveStatsLink) ? null : user.liveStatsLink;
        return new User(userId, user.foldingUserName, user.displayName, user.passkey, user.category, profileLink, liveStatsLink, user.hardware, user.team, user.userIsCaptain);
    }

    /**
     * Updates a {@link User} with the given {@link Hardware}.
     * <p>
     * If a {@link Hardware} has been updated, the {@link User} needs its reference updated too.
     *
     * @param user     the {@link User} to be updated with the new {@link Hardware}
     * @param hardware the updated {@link Hardware}
     * @return the updated {@link User}
     */
    public static User updateHardware(final User user, final Hardware hardware) {
        final String profileLink = isEmpty(user.profileLink) ? null : user.profileLink;
        final String liveStatsLink = isEmpty(user.liveStatsLink) ? null : user.liveStatsLink;
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, profileLink, liveStatsLink, hardware, user.team, user.userIsCaptain);
    }

    /**
     * Updates a {@link User} with the given {@link Team}.
     * <p>
     * If a {@link Team} has been updated, the {@link User} needs its reference updated too.
     *
     * @param user the {@link User} to be updated with the new {@link Team}
     * @param team the updated {@link Team}
     * @return the updated {@link User}
     */
    // TODO: [zodac] Implement
    public static User updateTeam(final User user, final Team team) {
        final String profileLink = isEmpty(user.profileLink) ? null : user.profileLink;
        final String liveStatsLink = isEmpty(user.liveStatsLink) ? null : user.liveStatsLink;
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, profileLink, liveStatsLink, user.hardware, team, user.userIsCaptain);
    }

    /**
     * Hides the {@code passkey} for the given {@link User}.
     * <p>
     * Since we do not want {@link User}s' passkeys to be made available through the REST API, we hide most of the passkey (we leave the first few digits visible).
     *
     * @param user the {@link User} whose passkey is to be hidden
     * @return the updated {@link User}
     */
    public static User hidePasskey(final User user) {
        final String profileLink = isEmpty(user.profileLink) ? null : user.profileLink;
        final String liveStatsLink = isEmpty(user.liveStatsLink) ? null : user.liveStatsLink;
        return new User(user.id, user.foldingUserName, user.displayName, hidePasskey(user.passkey), user.category, profileLink, liveStatsLink, user.hardware, user.team, user.userIsCaptain);
    }

    private static String hidePasskey(final String passkey) {
        final int endIndex = Math.min(PASSKEY_LENGTH_NOT_TO_HIDE, passkey.length()); // In case passkey has fewer than 8 characters
        return passkey.substring(0, endIndex).concat(PASSKEY_MASK);
    }

    private static boolean isEmpty(final String input) {
        return input == null || input.isBlank();
    }

    @Override
    public boolean isEqualRequest(final RequestPojo inputRequest) {
        if (!(inputRequest instanceof UserRequest)) {
            return false;
        }

        final UserRequest userRequest = (UserRequest) inputRequest;

        return id == userRequest.getId() &&
                hardware.getId() == userRequest.getHardwareId() &&
                team.getId() == userRequest.getTeamId() &&
                userIsCaptain == userRequest.isUserIsCaptain() &&
                Objects.equals(foldingUserName, userRequest.getFoldingUserName()) &&
                Objects.equals(displayName, userRequest.getDisplayName()) &&
                Objects.equals(passkey, userRequest.getPasskey()) &&
                Objects.equals(category.displayName(), userRequest.getCategory()) &&
                Objects.equals(profileLink, userRequest.getProfileLink()) &&
                Objects.equals(liveStatsLink, userRequest.getLiveStatsLink());
    }
}
