package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
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
    public static HardwareRequest generateHardware() {
        final String hardwareName = nextHardwareName();
        return HardwareRequest.builder()
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
    public static HardwareRequest generateHardwareWithId(final int hardwareId) {
        final String hardwareName = nextHardwareName();
        return HardwareRequest.builder()
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
    public static HardwareRequest generateHardwareWithMultiplier(final double multiplier) {
        final String hardwareName = nextHardwareName();
        return HardwareRequest.builder()
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
    public static HardwareRequest generateHardwareWithOperatingSystem(final OperatingSystem operatingSystem) {
        final String hardwareName = nextHardwareName();
        return HardwareRequest.builder()
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
    public static TeamRequest generateTeam() {
        return TeamRequest.builder()
                .teamName(nextTeamName())
                .build();
    }

    public static TeamRequest generateTeamWithId(final int teamId) {
        return TeamRequest.builder()
                .id(teamId)
                .teamName(nextTeamName())
                .build();
    }

    public static TeamRequest generateInvalidTeam() {
        return TeamRequest.builder()
                .teamName(nextTeamName())
                .forumLink("invalidLink")
                .build();
    }

    public static UserRequest generateUser() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static UserRequest generateUserWithCategory(final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(category.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static UserRequest generateUserWithId(final int userId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .id(userId)
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static UserRequest generateUserWithHardwareId(final int hardwareId) throws FoldingRestException {
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static UserRequest generateUserWithTeamId(final int teamId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static UserRequest generateUserWithTeamIdAndCategory(final int teamId, final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(category.displayName())
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }

    public static UserRequest generateUserWithLiveStatsLink(final String liveStatsLink) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
                .foldingUserName(userName)
                .displayName(userName)
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .liveStatsLink(liveStatsLink)
                .hardwareId(hardwareId)
                .teamId(teamId)
                .build();
    }
}
