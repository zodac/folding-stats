package me.zodac.folding.db.postgres;

import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.parsing.FoldingStats;

import java.util.List;

import static java.util.stream.Collectors.toList;

class PostgresSqlQueryBuilder {

    private PostgresSqlQueryBuilder() {

    }

    public static String insertHardware(final Hardware hardware) {
        return String.format("INSERT INTO hardware (hardware_name, display_name, multiplier) VALUES ('%s', '%s', %s) RETURNING hardware_id;", hardware.getHardwareName(), hardware.getDisplayName(), hardware.getMultiplier());
    }

    public static String getHardware() {
        return "SELECT * FROM hardware;";
    }

    public static String getHardware(final String hardwareId) {
        return String.format("SELECT * FROM hardware WHERE hardware_id = '%s';", hardwareId);
    }

    public static String insertFoldingUser(final FoldingUser foldingUser) {
        return String.format("INSERT INTO folding_users (folding_username, display_username, passkey, hardware_id) VALUES ('%s', '%s', '%s', %s) RETURNING user_id;", foldingUser.getFoldingUserName(), foldingUser.getDisplayName(), foldingUser.getPasskey(), foldingUser.getHardwareId());
    }

    public static String getFoldingUsers() {
        return "SELECT * FROM folding_users;";
    }

    public static String getFoldingUser(final String foldingUserId) {
        return String.format("SELECT * FROM folding_users WHERE user_id = '%s';", foldingUserId);
    }

    public static String insertFoldingTeam(final FoldingTeam foldingTeam) {
        return String.format("INSERT INTO folding_teams (team_name, captain_user_id, nvidia_gpu_user_id, amd_gpu_user_id, wildcard_user_id) VALUES ('%s', %s, %s, %s, %s) RETURNING team_id;", foldingTeam.getTeamName(), foldingTeam.getCaptainUserId(), foldingTeam.getNvidiaGpuUserId(), foldingTeam.getAmdGpuUserId(), foldingTeam.getWildcardUserId());
    }

    public static String getFoldingTeams() {
        return "SELECT * FROM folding_teams;";
    }

    public static String getFoldingTeam(final String foldingTeamId) {
        return String.format("SELECT * FROM folding_teams WHERE team_id = '%s';", foldingTeamId);
    }

    public static List<String> insertFoldingStats(final List<FoldingStats> foldingStats) {
        return foldingStats.stream()
                .map(foldingStatsForUser -> String.format("INSERT INTO individual_points (user_id, utc_timestamp, total_points) VALUES (%s, '%s', %s);",
                        foldingStatsForUser.getUserId(), foldingStatsForUser.getTimestamp(), foldingStatsForUser.getTotalPoints()))
                .collect(toList());
    }
}
