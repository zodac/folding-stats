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

import java.util.Objects;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.tc.request.UserRequest;

/**
 * POJO defining a single {@link User} Folding on a username/passkey combination to participate in the {@code Team Competition}.
 *
 * <p>
 * Ideally this username/passkey will only be used on a single piece of {@link Hardware}. Though we cannot verify
 * that externally, we only allow each {@link User} a single {@link Hardware} at a time.
 *
 * <p>
 * There is a limit on the number of users each team can have, defined by the {@link Category} description.
 *
 * <p>
 * Each {@link User} can join a {@link Team} in order to have their Folding@Home stats retrieved, and they can
 * contribute to the {@code Team Competition}.
 */
public record User(int id,
                   String foldingUserName,
                   String displayName,
                   String passkey,
                   Category category,
                   String profileLink,
                   String liveStatsLink,
                   Hardware hardware,
                   Team team,
                   boolean userIsCaptain
) implements ResponsePojo {

    /**
     * The default {@link User} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_USER_ID = 0;

    private static final int PASSKEY_LENGTH_NOT_TO_HIDE = 8;
    private static final String PASSKEY_MASK = "************************"; // 24 characters

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link User} from the DB response.
     *
     * @param userId          the ID
     * @param foldingUserName the Folding@Home username
     * @param displayName     the display name for the {@code Team Competition}
     * @param passkey         the Folding@Home passkey for this user
     * @param category        the {@link Category} the user is eligible for when added to a {@link Team}
     * @param profileLink     a URL linking to the {@link User}'s profile on their forum
     * @param liveStatsLink   a URL linking to the live Folding@Home stats (HFM, for example) for the {@link User}
     * @param hardware        the {@link Hardware} that this {@link User} is Folding on
     * @param team            the {@link Team} that the {@link User} is Folding for
     * @param isCaptain       whether the {@link User} is the captain of their {@link Team}
     * @return the created {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User create(final int userId,
                              final String foldingUserName,
                              final String displayName,
                              final String passkey,
                              final Category category,
                              final String profileLink,
                              final String liveStatsLink,
                              final Hardware hardware,
                              final Team team,
                              final boolean isCaptain) {
        if (hardware == null) {
            throw new IllegalArgumentException("'hardware' must not be null");
        }

        if (team == null) {
            throw new IllegalArgumentException("'team' must not be null");
        }

        final String profileLinkOrNull = StringUtils.isBlank(profileLink) ? null : profileLink;
        final String liveStatsLinkOrNull = StringUtils.isBlank(liveStatsLink) ? null : liveStatsLink;
        return new User(userId, foldingUserName, displayName, passkey, category, profileLinkOrNull, liveStatsLinkOrNull, hardware, team, isCaptain);
    }

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link User}, the {@link #EMPTY_USER_ID} will be used instead.
     *
     * @param foldingUserName the Folding@Home username
     * @param displayName     the display name for the {@code Team Competition}
     * @param passkey         the Folding@Home passkey for this user
     * @param category        the {@link Category} the user is eligible for when added to a {@link Team}
     * @param profileLink     a URL linking to the {@link User}'s profile on their forum
     * @param liveStatsLink   a URL linking to the live Folding@Home stats (HFM, for example) for the {@link User}
     * @param hardware        the {@link Hardware} that this {@link User} is Folding on
     * @param team            the {@link Team} that the {@link User} is Folding for
     * @param isCaptain       whether the {@link User} is the captain of their {@link Team}
     * @return the created {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User createWithoutId(final String foldingUserName,
                                       final String displayName,
                                       final String passkey,
                                       final Category category,
                                       final String profileLink,
                                       final String liveStatsLink,
                                       final Hardware hardware,
                                       final Team team,
                                       final boolean isCaptain) { // TODO: Use 'Role' enum for team membership
        return create(EMPTY_USER_ID, foldingUserName, displayName, passkey, category, profileLink, liveStatsLink, hardware, team, isCaptain);
    }

    /**
     * Creates a {@link User}.
     *
     * <p>
     * We assume the provided {@link UserRequest}'s {@link Category} has already been validated.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link User}, the {@link #EMPTY_USER_ID} will be used instead.
     *
     * @param userRequest the input {@link UserRequest} from the REST endpoint
     * @param hardware    the {@link Hardware} that this {@link User} is Folding on
     * @param team        the {@link Team} that the {@link User} is Folding for
     * @return the created {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User createWithoutId(final UserRequest userRequest, final Hardware hardware, final Team team) {
        return createWithoutId(
            userRequest.getFoldingUserName(),
            userRequest.getDisplayName(),
            userRequest.getPasskey(),
            Category.get(userRequest.getCategory()),
            userRequest.getProfileLink(),
            userRequest.getLiveStatsLink(),
            hardware,
            team,
            userRequest.isUserIsCaptain()
        );
    }

    /**
     * Updates a {@link User} with the given ID.
     *
     * <p>
     * Once the {@link User} has been persisted in the DB, we will know its ID. We create a new {@link User} instance with this ID,
     * which can be used to retrieval/referencing later.
     *
     * @param userId the DB-generated ID
     * @param user   the {@link User} to be updated with the ID
     * @return the updated {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User updateWithId(final int userId, final User user) {
        return create(userId, user.foldingUserName, user.displayName, user.passkey, user.category, user.profileLink, user.liveStatsLink,
            user.hardware, user.team, user.userIsCaptain);
    }

    /**
     * Creates a new {@link User} with the given {@link Hardware}.
     *
     * <p>
     * If a {@link Hardware} has been updated, the {@link User} needs its reference updated too.
     *
     * @param user     the {@link User} to be updated with the new {@link Hardware}
     * @param hardware the updated {@link Hardware}
     * @return the updated {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User updateHardware(final User user, final Hardware hardware) {
        return create(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.profileLink, user.liveStatsLink, hardware,
            user.team, user.userIsCaptain);
    }

    /**
     * Creates a new {@link User} with the given {@link Team}.
     *
     * <p>
     * If a {@link Team} has been updated, the {@link User} needs its reference updated too.
     *
     * @param user the {@link User} to be updated with the new {@link Team}
     * @param team the updated {@link Team}
     * @return the updated {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User updateTeam(final User user, final Team team) {
        return create(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.profileLink, user.liveStatsLink,
            user.hardware, team, user.userIsCaptain);
    }

    /**
     * Sets the {@link User} to no longer be captain of their {@link Team}.
     *
     * @param user the {@link User} to update
     * @return the updated {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User removeCaptaincyFromUser(final User user) {
        return create(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.profileLink, user.liveStatsLink,
            user.hardware, user.team, false);
    }

    /**
     * Hides the {@code passkey} for the given {@link User}.
     *
     * <p>
     * Since we do not want {@link User} passkeys to be made available through the REST API, we hide most of the passkey, though we leave the first
     * eight (8) digits visible.
     *
     * @param user the {@link User} whose passkey is to be hidden
     * @return the updated {@link User}
     */
    public static User hidePasskey(final User user) {
        return create(user.id, user.foldingUserName, user.displayName, hidePasskey(user.passkey), user.category, user.profileLink, user.liveStatsLink,
            user.hardware, user.team, user.userIsCaptain);
    }

    private static String hidePasskey(final String passkey) {
        final int endIndex = Math.min(PASSKEY_LENGTH_NOT_TO_HIDE, passkey.length()); // In case passkey has fewer than 8 characters, though unlikely
        return passkey.substring(0, endIndex) + PASSKEY_MASK;
    }

    /**
     * Checks if the input {@link UserRequest} is equal to the {@link User}.
     *
     * <p>
     * While the {@link UserRequest} will likely not be a complete match, there should be enough fields to verify
     * if it is the same as an existing {@link User}.
     *
     * @param userRequest input {@link UserRequest}
     * @return {@code true} if the input{@link UserRequest} is equal to the {@link User}
     */
    public boolean isEqualRequest(final UserRequest userRequest) {
        return hardware.id() == userRequest.getHardwareId()
            && team.id() == userRequest.getTeamId()
            && userIsCaptain == userRequest.isUserIsCaptain()
            && Objects.equals(foldingUserName, userRequest.getFoldingUserName())
            && Objects.equals(displayName, userRequest.getDisplayName())
            && Objects.equals(passkey, userRequest.getPasskey())
            && Objects.equals(category.toString(), userRequest.getCategory())
            && Objects.equals(profileLink, userRequest.getProfileLink())
            && Objects.equals(liveStatsLink, userRequest.getLiveStatsLink());
    }
}
