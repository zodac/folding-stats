package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.test.utils.rest.request.HardwareUtils;
import me.zodac.folding.test.utils.rest.request.TeamUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class used to generate {@link Hardware}, {@link User} and {@link Team} instances for test cases.
 */
public final class TestGenerator {

    private static final AtomicInteger HARDWARE_COUNT = new AtomicInteger(1);
    private static final AtomicInteger USER_COUNT = new AtomicInteger(1);
    private static final AtomicInteger TEAM_COUNT = new AtomicInteger(1);

    private TestGenerator() {

    }

    public static String nextHardwareName() {
        return "DummyHardware" + HARDWARE_COUNT.getAndIncrement();
    }

    public static String nextTeamName() {
        return "DummyTeam" + TEAM_COUNT.getAndIncrement();
    }

    public static String nextUserName() {
        return "DummyUser" + USER_COUNT.getAndIncrement();
    }

    /**
     * Generate a {@link Hardware} with a {@link Hardware#getMultiplier()} of <b>x1</b>.
     *
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardware() {
        final String hardwareName = nextHardwareName();
        return Hardware.builder()
                .hardwareName(hardwareName)
                .displayName(hardwareName)
                .operatingSystem(OperatingSystem.WINDOWS.displayName())
                .multiplier(1.0D)
                .build();
    }
//

    /**
     * Generate a {@link Hardware} with a {@link Hardware#getMultiplier()} of <b>x1</b>.
     *
     * @param hardwareId the ID of the {@link Hardware}
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardwareWithId(final int hardwareId) {
        final String hardwareName = nextHardwareName();
        return Hardware.builder()
                .id(hardwareId)
                .hardwareName(hardwareName)
                .displayName(hardwareName).operatingSystem(OperatingSystem.WINDOWS.displayName())
                .multiplier(1.0D)
                .build();
    }

    /**
     * Generate a {@link Hardware}.
     *
     * @param multiplier the multiplier to be applied to the {@link Hardware}
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardwareWithMultiplier(final double multiplier) {
        final String hardwareName = nextHardwareName();
        return Hardware.builder()
                .hardwareName(hardwareName)
                .displayName(hardwareName)
                .operatingSystem(OperatingSystem.WINDOWS.displayName())
                .multiplier(multiplier)
                .build();
    }

    /**
     * Generate a {@link Hardware}.
     *
     * @param operatingSystem the {@link OperatingSystem} of the {@link Hardware}
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardwareWithOperatingSystem(final OperatingSystem operatingSystem) {
        final String hardwareName = nextHardwareName();
        return Hardware.builder()
                .hardwareName(hardwareName)
                .displayName(hardwareName)
                .operatingSystem(operatingSystem.displayName())
                .multiplier(1.0D)
                .build();
    }


    /**
     * Generates a {@link Team}.
     *
     * @return the generated {@link Team}
     */
    public static Team generateTeam() {
        return Team.builder()
                .teamName(nextTeamName())
                .build();
    }

    public static Team generateTeamWithId(final int teamId) {
        return Team.builder()
                .id(teamId)
                .teamName(nextTeamName())
                .build();
    }

    public static Team generateInvalidTeam() {
        return Team.builder()
                .teamName(nextTeamName())
                .forumLink("invalidLink")
                .build();
    }

    public static User generateUser() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final String userName = nextUserName();

        return User.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static User generateUserWithCategory(final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final String userName = nextUserName();

        return User.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(category.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static User generateUserWithId(final int userId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final String userName = nextUserName();

        return User.builder()
                .id(userId)
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static User generateUserWithHardwareId(final int hardwareId) throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final String userName = nextUserName();

        return User.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static User generateUserWithTeamId(final int teamId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final String userName = nextUserName();

        return User.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static User generateUserWithTeamIdAndCategory(final int teamId, final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final String userName = nextUserName();

        return User.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(category.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static User generateUserWithLiveStatsLink(final String liveStatsLink) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final String userName = nextUserName();

        return User.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .liveStatsLink(liveStatsLink)
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }
//
////    public static User generateCaptain() throws FoldingRestException {
////        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
////        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
////        return generateUser(hardwareId, teamId, true);
////    }
//
//    private static User generateUser(final int hardwareId, final int teamId, final boolean isCaptain) {
//        return User.createWithoutId(
//                "Dummy_User" + userCount++,
//                "Dummy User",
//                "DummyPasskey12345678901234567890",
//                Category.AMD_GPU,
//                "",
//                "",
//                hardwareId,
//                teamId,
//                isCaptain);
//    }
//
//    /**
//     * Generates a {@link User} with the supplied {@link Hardware} ID.
//     *
//     * @param hardwareId the ID of the {@link Hardware} for the user
//     * @return the generated {@link User}
//     */
//    public static User generateUserWithHardwareId(final int hardwareId) {
//        return User.createWithoutId(
//                "Dummy_User" + userCount,
//                "Dummy User" + userCount++,
//                "DummyPasskey12345678901234567890",
//                Category.NVIDIA_GPU,
//                hardwareId,
//                "",
//                "",
//                false);
//    }
//
//    /**
//     * Generates a {@link User} with the supplied {@link Category} linked with an auto-created {@link Hardware}.
//     *
//     * @param category the {@link Category} of the {@link Hardware} for the user
//     * @return the generated {@link User}
//     * @throws FoldingRestException thrown if an error occurs creating the {@link Hardware}
//     * @see #generateHardware()
//     */
//    public static User generateUserWithCategory(final Category category) throws FoldingRestException {
//        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
//        return User.createWithoutId(
//                "Dummy_User" + userCount,
//                "Dummy User" + userCount++,
//                "DummyPasskey12345678901234567890",
//                category,
//                hardwareId,
//                "",
//                "",
//                false);
//    }
//
//    /**
//     * Generates a {@link User} with the supplied {@link User} ID linked with an auto-created {@link Hardware}.
//     *
//     * @param userId the ID of the {@link User}
//     * @return the generated {@link User}
//     * @throws FoldingRestException thrown if an error occurs creating the {@link Hardware}
//     * @see #generateHardware()
//     */
//    public static User generateUserWithUserId(final int userId) throws FoldingRestException {
//        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
//        return User.create(
//                userId,
//                "Dummy_User" + userCount,
//                "Dummy User" + userCount++,
//                "DummyPasskey12345678901234567890",
//                Category.NVIDIA_GPU,
//                hardwareId,
//                "",
//                "",
//                false);
//    }
}
