package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.rest.api.tc.UserResult;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserSummary {

    private String displayName;
    private String foldingName;
    private String teamName;
    private long points;
    private long multipliedPoints;
    private int units;

    private long diffToLeader;
    private long diffToNext;

    public static UserSummary create(final UserResult userResult, final String teamName, final long diffToLeader, final long diffToNext) {
        return new UserSummary(userResult.getDisplayName(), userResult.getFoldingName(), teamName, userResult.getPoints(), userResult.getMultipliedPoints(), userResult.getUnits(), diffToLeader, diffToNext);
    }

    public static UserSummary createLeader(final UserResult userResult, final String teamName) {
        return create(userResult, teamName, 0L, 0L);
    }
}
