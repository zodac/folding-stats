package me.zodac.folding.db.postgres;

import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.HardwareCategory;
import me.zodac.folding.parsing.FoldingStats;

import java.util.List;

import static java.util.stream.Collectors.toList;

class PostgresSqlQueryBuilder {

    private PostgresSqlQueryBuilder() {

    }

    public static String insertHardwareCategory(final HardwareCategory hardwareCategory) {
        return String.format("INSERT INTO hardware_categories (category_name, multiplier) VALUES ('%s', '%s') RETURNING hardware_id;", hardwareCategory.getCategoryName(), hardwareCategory.getMultiplier());
    }

    public static String getHardwareCategories() {
        return "SELECT * FROM hardware_categories;";
    }

    public static String getHardwareCategory(final String hardwareId) {
        return String.format("SELECT * FROM hardware_categories WHERE hardware_id = '%s';", hardwareId);
    }

    public static String insertFoldingUser(final FoldingUser foldingUser) {
        return String.format("INSERT INTO folding_users (folding_username, display_username, passkey, hardware_id, hardware_name) VALUES ('%s', '%s', '%s', '%s', '%s') RETURNING user_id;", foldingUser.getFoldingUserName(), foldingUser.getDisplayName(), foldingUser.getPasskey(), foldingUser.getHardwareCategoryId(), foldingUser.getHardwareName());
    }

    public static String getFoldingUsers() {
        return "SELECT * FROM folding_users;";
    }

    public static String getFoldingUser(final String foldingUserId) {
        return String.format("SELECT * FROM folding_users WHERE user_id = '%s';", foldingUserId);
    }

    public static List<String> insertFoldingStats(final List<FoldingStats> foldingStats) {
        return foldingStats.stream()
                .map(foldingStatsForUser -> String.format("INSERT INTO individual_points (user_id, utc_timestamp, total_points) VALUES ('%s', '%s', '%s');",
                        foldingStatsForUser.getUserId(), foldingStatsForUser.getTimestamp(), foldingStatsForUser.getTotalPoints()))
                .collect(toList());
    }
}
