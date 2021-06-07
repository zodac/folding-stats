package me.zodac.folding.api.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.User;

/**
 * Simple POJO encapsulating the username and passkey for a Folding@Home user.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class FoldingStatsDetails {

    private final String foldingUserName;
    private final String passkey;

    /**
     * Creates a {@link FoldingStatsDetails}.
     *
     * @param foldingUserName the Folding@Home username
     * @param passkey         the passkey for the user
     * @return the created {@link FoldingStatsDetails}
     */
    public static FoldingStatsDetails create(final String foldingUserName, final String passkey) {
        return new FoldingStatsDetails(foldingUserName, passkey);
    }

    /**
     * Creates a {@link FoldingStatsDetails}.
     *
     * @param user the {@link User} from which we can get the Folding@Home username and passkey
     * @return the created {@link FoldingStatsDetails}
     */
    public static FoldingStatsDetails createFromUser(final User user) {
        return new FoldingStatsDetails(user.getFoldingUserName(), user.getPasskey());
    }
}
