/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.test.integration.util;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;

/**
 * Utility class used to generate {@link HardwareRequest}, {@link TeamRequest} and {@link UserRequest} instances for test cases.
 */
public final class DummyDataGenerator {

    private static final AtomicInteger HARDWARE_COUNT = new AtomicInteger(1);
    private static final AtomicInteger USER_COUNT = new AtomicInteger(1);
    private static final AtomicInteger TEAM_COUNT = new AtomicInteger(1);

    private DummyDataGenerator() {

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
            .averagePpd(1L)
            .build();
    }

    /**
     * Generate a {@link HardwareRequest} with the {@link HardwareMake} and {@link HardwareType} derived from the provided {@link Category}.
     *
     * @param category the {@link Category} to define the {@link HardwareMake} and {@link HardwareType}
     * @return the generated {@link HardwareRequest}
     */
    public static HardwareRequest generateHardwareFromCategory(final Category category) {
        final String hardwareName = nextHardwareName();
        final HardwareMake hardwareMake = new ArrayList<>(category.supportedHardwareMakes()).get(0);
        final HardwareType hardwareType = new ArrayList<>(category.supportedHardwareTypes()).get(0);

        return HardwareRequest.builder()
            .hardwareName(hardwareName)
            .displayName(hardwareName)
            .hardwareMake(hardwareMake.toString())
            .hardwareType(hardwareType.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
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
            .averagePpd(1L)
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
     * Generates a {@link UserRequest}.
     *
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUser() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final int teamId = TeamUtils.create(generateTeam()).id();
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
     * Generates a {@link UserRequest} where the {@link me.zodac.folding.api.tc.User} is captain of their {@link me.zodac.folding.api.tc.Team}.
     *
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateCaptainUser() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final int teamId = TeamUtils.create(generateTeam()).id();
        final String userName = nextUserName();

        return UserRequest.builder()
            .foldingUserName(userName)
            .displayName(userName)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .hardwareId(hardwareId)
            .teamId(teamId)
            .userIsCaptain(true)
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
        final int teamId = TeamUtils.create(generateTeam()).id();
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
     */
    public static UserRequest generateUserWithTeamId(final int teamId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
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
     * Generates a {@link UserRequest} with a specified {@link me.zodac.folding.api.tc.Hardware} ID and {@link me.zodac.folding.api.tc.Team} ID.
     *
     * @param hardwareId the {@link me.zodac.folding.api.tc.Hardware} ID
     * @param teamId     the {@link me.zodac.folding.api.tc.Team} ID
     * @return the generated {@link UserRequest}
     */
    public static UserRequest generateUserWithHardwareIdAndTeamId(final int hardwareId, final int teamId) {
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
        final int hardwareId = HardwareUtils.create(generateHardwareFromCategory(category)).id();
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
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final int teamId = TeamUtils.create(generateTeam()).id();
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
