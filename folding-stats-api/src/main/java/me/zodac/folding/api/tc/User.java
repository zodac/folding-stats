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

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.checkerframework.nullaway.checker.nullness.qual.Nullable;

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
@Getter
@Accessors(fluent = true)
@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public final class User implements ResponsePojo {

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
    private final @Nullable String profileLink;
    private final @Nullable String liveStatsLink;
    private final Hardware hardware;
    private final Team team;
    private final Role role;

    /**
     * Constructor.
     *
     * @param id              the ID
     * @param foldingUserName the Folding@Home username
     * @param displayName     the display name for the {@code Team Competition}
     * @param passkey         the Folding@Home passkey for this user
     * @param category        the {@link Category} the user is eligible for when added to a {@link Team}
     * @param profileLink     a URL linking to the {@link User}'s profile on their forum
     * @param liveStatsLink   a URL linking to the live Folding@Home stats (HFM, for example) for the {@link User}
     * @param hardware        the {@link Hardware} that this {@link User} is Folding on
     * @param team            the {@link Team} that the {@link User} is Folding for
     * @param role            the {@link User}'s role in their {@link Team}
     */
    public User(final int id, final String foldingUserName, final String displayName, final String passkey, final Category category,
                @Nullable final String profileLink, @Nullable final String liveStatsLink, final Hardware hardware, final Team team, final Role role) {
        this.id = id;
        this.foldingUserName = foldingUserName;
        this.displayName = displayName;
        this.passkey = passkey;
        this.category = category;
        this.profileLink = profileLink;
        this.liveStatsLink = liveStatsLink;
        this.hardware = hardware;
        this.team = team;
        this.role = role;
    }

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
     * @param role            the {@link User}'s role in their {@link Team}
     * @return the created {@link User}
     * @throws IllegalArgumentException thrown if {@code hardware} or {@code team} is null
     */
    public static User create(final int userId,
                              final String foldingUserName,
                              final String displayName,
                              final String passkey,
                              final Category category,
                              final @Nullable String profileLink,
                              final @Nullable String liveStatsLink,
                              final Hardware hardware,
                              final Team team,
                              final Role role) {
        final String unescapedDisplayName = StringUtils.unescapeHtml(displayName);
        final String profileLinkOrNull = StringUtils.isBlank(profileLink) ? null : profileLink;
        final String liveStatsLinkOrNull = StringUtils.isBlank(liveStatsLink) ? null : liveStatsLink;
        return new User(userId, foldingUserName, unescapedDisplayName, passkey, category, profileLinkOrNull, liveStatsLinkOrNull, hardware, team,
            role);
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
        return create(
            EMPTY_USER_ID,
            userRequest.foldingUserName(),
            userRequest.displayName(),
            userRequest.passkey(),
            Category.get(userRequest.category()),
            userRequest.profileLink(),
            userRequest.liveStatsLink(),
            hardware,
            team,
            userRequest.userIsCaptain() ? Role.CAPTAIN : Role.MEMBER
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
            user.hardware, user.team, user.role);
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
            user.team, user.role);
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
            user.hardware, team, user.role);
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
            user.hardware, user.team, Role.MEMBER);
    }

    /**
     * Checks if the {@link User}'s {@code passkey} has been removed/hidden with a mask, or is exposed as plain-text.
     *
     * @return {@code true} if the {@code passkey} is hidden
     */
    public boolean isPasskeyHidden() {
        return StringUtils.isBlank(passkey) || passkey.contains(PASSKEY_MASK);
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
            user.hardware, user.team, user.role);
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
        return hardware.id() == userRequest.hardwareId()
            && team.id() == userRequest.teamId()
            && ((userRequest.userIsCaptain() && role == Role.CAPTAIN) || (role == Role.MEMBER))
            && Objects.equals(foldingUserName, userRequest.foldingUserName())
            && Objects.equals(displayName, userRequest.displayName())
            && Objects.equals(passkey, userRequest.passkey())
            && Objects.equals(category.toString(), userRequest.category())
            && Objects.equals(profileLink, userRequest.profileLink())
            && Objects.equals(liveStatsLink, userRequest.liveStatsLink());
    }
}
