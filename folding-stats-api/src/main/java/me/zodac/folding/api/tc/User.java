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
 * POJO defining a single {@link User} Folding on a username+passkey combination to participate in the <code>Team Competition</code>. Ideally this username+passkey will only be used on a single piece of {@link Hardware}.
 * Though we cannot verify that externally, we only allow each {@link User} a single {@link Hardware} at a time.
 * <p>
 * Each {@link User} can join a {@link Team} in order to have their Folding@Home stats retrieved, and they can contribute to the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class User implements Identifiable {

    /**
     * The default {@link User} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_USER_ID = 0;

    private static final int PASSKEY_LENGTH_NOT_TO_HIDE = 8;
    private static final String PASSKEY_MASK = "************************"; // 24 characters

    private int id;
    private String foldingUserName;
    private String displayName;
    private String passkey;
    private String category;
    private String profileLink;
    private String liveStatsLink;
    private int hardwareId;
    private int teamId;
    private boolean userIsCaptain;

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
     * @param hardwareId      the ID of the {@link Hardware} that this {@link User} is Folding on
     * @param teamId          the ID of the {@link Team} that the {@link User} is Folding for
     * @param isCaptain       whether the {@link User} is the captain of their {@link Team}
     * @return the created {@link User}
     */
    public static User create(final int userId, final String foldingUserName, final String displayName, final String passkey, final Category category, final String profileLink, final String liveStatsLink, final int hardwareId, final int teamId, final boolean isCaptain) {
        final String profileLinkOrNull = isEmpty(profileLink) ? null : profileLink;
        final String liveStatsLinkOrNull = isEmpty(liveStatsLink) ? null : liveStatsLink;
        return new User(userId, foldingUserName, displayName, passkey, category.displayName(), profileLinkOrNull, liveStatsLinkOrNull, hardwareId, teamId, isCaptain);
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
     * @param hardwareId      the ID of the {@link Hardware} that this {@link User} is Folding on
     * @param teamId          the ID of the {@link Team} that the {@link User} is Folding for
     * @param isCaptain       whether the {@link User} is the captain of their {@link Team}
     * @return the created {@link User}
     */
    public static User createWithoutId(final String foldingUserName, final String displayName, final String passkey, final Category category, final String profileLink, final String liveStatsLink, final int hardwareId, final int teamId, final boolean isCaptain) {
        final String profileLinkOrNull = isEmpty(profileLink) ? null : profileLink;
        final String liveStatsLinkOrNull = isEmpty(liveStatsLink) ? null : liveStatsLink;
        return new User(EMPTY_USER_ID, foldingUserName, displayName, passkey, category.displayName(), profileLinkOrNull, liveStatsLinkOrNull, hardwareId, teamId, isCaptain);
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
        return new User(userId, user.foldingUserName, user.displayName, user.passkey, user.category, profileLink, liveStatsLink, user.hardwareId, user.teamId, user.userIsCaptain);
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
        return new User(user.id, user.foldingUserName, user.displayName, hidePasskey(user.passkey), user.category, profileLink, liveStatsLink, user.hardwareId, user.teamId, user.userIsCaptain);
    }

    private static String hidePasskey(final String passkey) {
        final int endIndex = Math.min(PASSKEY_LENGTH_NOT_TO_HIDE, passkey.length()); // In case passkey has fewer than 8 characters
        return passkey.substring(0, endIndex).concat(PASSKEY_MASK);
    }

    private static boolean isEmpty(final String input) {
        return input == null || input.isBlank();
    }
}
