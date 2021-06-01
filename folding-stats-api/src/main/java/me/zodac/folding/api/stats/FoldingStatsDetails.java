package me.zodac.folding.api.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.User;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class FoldingStatsDetails {

    private final String foldingUserName;
    private final String passkey;

    public static FoldingStatsDetails create(final String foldingUserName, final String passkey) {
        return new FoldingStatsDetails(foldingUserName, passkey);
    }

    public static FoldingStatsDetails createFromUser(final User user) {
        return new FoldingStatsDetails(user.getFoldingUserName(), user.getPasskey());
    }
}
