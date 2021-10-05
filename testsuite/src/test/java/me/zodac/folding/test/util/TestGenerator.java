package me.zodac.folding.test.util;

import java.util.concurrent.atomic.AtomicInteger;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.util.rest.request.HardwareUtils;
import me.zodac.folding.test.util.rest.request.TeamUtils;

/**
 * Utility class used to generate {@link HardwareRequest}, {@link TeamRequest} and {@link UserRequest} instances for
 * test cases.
 */
public final class TestGenerator {

    private static final AtomicInteger HARDWARE_COUNT = new AtomicInteger(1);
    private static final AtomicInteger USER_COUNT = new AtomicInteger(1);
    private static final AtomicInteger TEAM_COUNT = new AtomicInteger(1);

    private TestGenerator() {

    }

    /**
     * Gets an incremented {@link HardwareRequest} name.
     *
     * @return the {@link HardwareRequest} name
     */
    public static String nextHardwareName() {
        return "DummyHardware" + HARDWARE_COUNT.getAndIncrement();
    }

    /**
     * Gets an incremented {@link TeamRequest} name.
     *
     * @return the {@link TeamRequest} name
     */
    public static String nextTeamName() {
        return "DummyTeam" + TEAM_COUNT.getAndIncrement();
    }

    /**
     * Gets an incremented {@link UserRequest} name.
     *
     * @return the {@link UserRequest} name
     */
    public static String nextUserName() {
        return "DummyUser" + USER_COUNT.getAndIncrement();
    }

    /**
     * Generate a {@link HardwareRequest} with a multiplier of <b>x1</b>.
     *
     * @return the generated {@link HardwareRequest}
     */
    public static HardwareRequest generateHardware() {
        final String hardwareName = nextHardwareName();
        return HardwareRequest.builder()
            .hardwareName(hardwareName)
            .displayName(hardwareName)
            .hardwareMake(HardwareMake.NVIDIA.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1.00D)
            .build();
    }

    /**
     * Generate a {@link HardwareRequest}.
     *
     * @param multiplier the multiplier to be applied to the {@link HardwareRequest}
     * @return the generated {@link HardwareRequest}
     */
    public static HardwareRequest generateHardwareWithMultiplier(final double multiplier) {
        final String hardwareName = nextHardwareName();
        return HardwareRequest.builder()
            .hardwareName(hardwareName)
            .displayName(hardwareName)
            .hardwareMake(HardwareMake.NVIDIA.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(multiplier)
            .averagePpd(1.00D)
            .build();
    }

    /**
     * Generates a {@link TeamRequest}.
     *
     * @return the generated {@link TeamRequest}
     */
    public static TeamRequest generateTeam() {
        return TeamRequest.builder()
            .teamName(nextTeamName())
            .build();
    }

    /**
     * Generates an invalid {@link TeamRequest}.
     *
     * <p>
     * Uses an invalid URL as the forum link, so validation will fail.
     *
     * @return the generated {@link TeamRequest}
     */
    public static TeamRequest generateInvalidTeam() {
        return TeamRequest.builder()
            .teamName(nextTeamName())
            .forumLink("invalidLink")
            .build();
    }

    /**
     * Generates a {@link UserRequest}.
     *
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUser() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .hardwareId(hardwareId)
            .teamId(teamId)
            .build();
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link Category}.
     *
     * @param category the {@link Category} of the {@link UserRequest}
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithCategory(final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(category.toString())
            .hardwareId(hardwareId)
            .teamId(teamId)
            .build();
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link me.zodac.folding.api.tc.Hardware} ID.
     *
     * @param hardwareId the {@link me.zodac.folding.api.tc.Hardware} ID
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithHardwareId(final int hardwareId) throws FoldingRestException {
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .hardwareId(hardwareId)
            .teamId(teamId)
            .build();
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link me.zodac.folding.api.tc.Team} ID.
     *
     * @param teamId the {@link me.zodac.folding.api.tc.Team} ID
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithTeamId(final int teamId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .hardwareId(hardwareId)
            .teamId(teamId)
            .build();
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link me.zodac.folding.api.tc.Team} ID and {@link Category}.
     *
     * @param teamId   the {@link me.zodac.folding.api.tc.Team} ID
     * @param category the {@link Category} of the {@link UserRequest}
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithTeamIdAndCategory(final int teamId, final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(category.toString())
            .hardwareId(hardwareId)
            .teamId(teamId)
            .build();
    }

    /**
     * Generates a {@link UserRequest} with a specified live stats link.
     *
     * @param liveStatsLink the {@link me.zodac.folding.api.tc.User} live stats link
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithLiveStatsLink(final String liveStatsLink) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final int teamId = TeamUtils.create(generateTeam()).getId();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .liveStatsLink(liveStatsLink)
            .hardwareId(hardwareId)
            .teamId(teamId)
            .build();
    }
}
