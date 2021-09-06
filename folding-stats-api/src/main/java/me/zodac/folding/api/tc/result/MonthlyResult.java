package me.zodac.folding.api.tc.result;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;

/**
 * POJO containing the overall result for a single month of the <code>Team Competition</code>.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class MonthlyResult {

    private final List<TeamLeaderboardEntry> teamLeaderboard;
    private final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard;

    /**
     * Creates a {@link MonthlyResult}.
     *
     * @param teamLeaderboard         the leaderboard for {@link me.zodac.folding.api.tc.Team}s
     * @param userCategoryLeaderboard the leaderboard for {@link me.zodac.folding.api.tc.User} {@link Category}s
     * @return the created {@link MonthlyResult}
     */
    public static MonthlyResult create(final List<TeamLeaderboardEntry> teamLeaderboard,
                                       final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard) {
        return new MonthlyResult(teamLeaderboard, userCategoryLeaderboard);
    }

    /**
     * Creates an empty {@link MonthlyResult}.
     *
     * @return the empty {@link MonthlyResult}
     */
    public static MonthlyResult empty() {
        final Map<Category, List<UserCategoryLeaderboardEntry>> emptyCategoryResult = new HashMap<>(Category.getAllValues().size());
        for (final Category category : Category.getAllValues()) {
            emptyCategoryResult.put(category, Collections.emptyList());
        }
        
        return new MonthlyResult(Collections.emptyList(), emptyCategoryResult);
    }
}
