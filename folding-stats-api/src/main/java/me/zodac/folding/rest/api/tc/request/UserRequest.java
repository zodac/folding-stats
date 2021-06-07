package me.zodac.folding.rest.api.tc.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.RequestPojo;

/**
 * POJO defining a single {@link UserRequest} Folding on a username/passkey combination to participate in the
 * <code>Team Competition</code>.
 * <p>
 * Ideally this username/passkey will only be used on a single piece of {@link me.zodac.folding.api.tc.Hardware}. Though we cannot verify
 * that externally, we only allow each {@link UserRequest} a single {@link me.zodac.folding.api.tc.Hardware} at a time.
 * <p>
 * There is a limit on the number of users each team can have, defined by the {@link me.zodac.folding.api.tc.Category}
 * description.
 * <p>
 * Each {@link UserRequest} can join a {@link me.zodac.folding.api.tc.Team} in order to have their Folding@Home stats retrieved, and they can
 * contribute to the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserRequest implements RequestPojo {

    /**
     * The default {@link UserRequest} ID. Since the REST request would not know the ID until the DB has created the object,
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
}
