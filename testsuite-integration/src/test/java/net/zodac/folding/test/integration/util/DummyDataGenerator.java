/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.test.integration.util;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.tc.request.HardwareRequest;
import net.zodac.folding.rest.api.tc.request.TeamRequest;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import net.zodac.folding.test.integration.util.rest.request.TeamUtils;
import org.jspecify.annotations.Nullable;

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

        return new HardwareRequest(
            hardwareName,
            hardwareName,
            HardwareMake.NVIDIA.toString(),
            HardwareType.GPU.toString(),
            1.00D,
            1L
        );
    }

    /**
     * Generate a {@link HardwareRequest} with the {@link HardwareMake} and {@link HardwareType} derived from the provided {@link Category}.
     *
     * @param category the {@link Category} to define the {@link HardwareMake} and {@link HardwareType}
     * @return the generated {@link HardwareRequest}
     */
    public static HardwareRequest generateHardwareFromCategory(final Category category) {
        final String hardwareName = nextHardwareName();
        final HardwareMake hardwareMake = new ArrayList<>(category.supportedHardwareMakes()).getFirst();
        final HardwareType hardwareType = new ArrayList<>(category.supportedHardwareTypes()).getFirst();

        return new HardwareRequest(
            hardwareName,
            hardwareName,
            hardwareMake.toString(),
            hardwareType.toString(),
            1.00D,
            1L
        );
    }

    /**
     * Generate a {@link HardwareRequest}.
     *
     * @param multiplier the multiplier to be applied to the {@link HardwareRequest}
     * @return the generated {@link HardwareRequest}
     */
    public static HardwareRequest generateHardwareWithMultiplier(final double multiplier) {
        final String hardwareName = nextHardwareName();
        return new HardwareRequest(
            hardwareName,
            hardwareName,
            HardwareMake.NVIDIA.toString(),
            HardwareType.GPU.toString(),
            multiplier,
            1L
        );
    }

    /**
     * Generates a {@link TeamRequest}.
     *
     * @return the generated {@link TeamRequest}
     */
    public static TeamRequest generateTeam() {
        return generateTeamWithName(nextTeamName());
    }

    /**
     * Generates a {@link TeamRequest}.
     *
     * @param teamName the name
     * @return the generated {@link TeamRequest}
     */
    public static TeamRequest generateTeamWithName(final String teamName) {
        return new TeamRequest(teamName, null, null);
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

        return generateUserRequest(
            userName,
            Category.NVIDIA_GPU,
            null,
            hardwareId,
            teamId,
            false
        );
    }

    /**
     * Generates a {@link UserRequest} where the {@link User} is captain of their {@link Team}.
     *
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateCaptain() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final int teamId = TeamUtils.create(generateTeam()).id();
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            Category.NVIDIA_GPU,
            null,
            hardwareId,
            teamId,
            true
        );
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link Hardware} ID.
     *
     * @param hardwareId the {@link Hardware} ID
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithHardwareId(final int hardwareId) throws FoldingRestException {
        final int teamId = TeamUtils.create(generateTeam()).id();
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            Category.NVIDIA_GPU,
            null,
            hardwareId,
            teamId,
            false
        );
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link Team} ID.
     *
     * @param teamId the {@link Team} ID
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     */
    public static UserRequest generateUserWithTeamId(final int teamId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            Category.NVIDIA_GPU,
            null,
            hardwareId,
            teamId,
            false
        );
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link Hardware} ID and {@link Team} ID.
     *
     * @param hardwareId the {@link Hardware} ID
     * @param teamId     the {@link Team} ID
     * @return the generated {@link UserRequest}
     */
    public static UserRequest generateUserWithHardwareIdAndTeamId(final int hardwareId, final int teamId) {
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            Category.NVIDIA_GPU,
            null,
            hardwareId,
            teamId,
            false
        );
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link Team} ID and {@link Category}.
     *
     * @param teamId   the {@link Team} ID
     * @param category the {@link Category} of the {@link UserRequest}
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithTeamIdAndCategory(final int teamId, final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardwareFromCategory(category)).id();
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            category,
            null,
            hardwareId,
            teamId,
            false
        );
    }

    /**
     * Generates a {@link UserRequest} with a specified {@link Team} ID and {@link Category} for a captain.
     *
     * @param teamId   the {@link Team} ID
     * @param category the {@link Category} of the {@link UserRequest}
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateCaptainWithTeamIdAndCategory(final int teamId, final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardwareFromCategory(category)).id();
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            category,
            null,
            hardwareId,
            teamId,
            true
        );
    }

    /**
     * Generates a {@link UserRequest} with a specified live stats link.
     *
     * @param liveStatsLink the {@link User} live stats link
     * @return the generated {@link UserRequest}
     * @throws FoldingRestException thrown if an error occurs executing {@link HardwareUtils#create(HardwareRequest)}
     *                              or {@link TeamUtils#create(TeamRequest)}
     */
    public static UserRequest generateUserWithLiveStatsLink(final String liveStatsLink) throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final int teamId = TeamUtils.create(generateTeam()).id();
        final String userName = nextUserName();

        return generateUserRequest(
            userName,
            Category.NVIDIA_GPU,
            liveStatsLink,
            hardwareId,
            teamId,
            false
        );
    }

    private static UserRequest generateUserRequest(final String userName,
                                                   final Category category,
                                                   final @Nullable String liveStatsLink,
                                                   final int hardwareId,
                                                   final int teamId,
                                                   final boolean isCaptain) {
        return new UserRequest(
            userName,
            userName,
            "DummyPasskey12345678901234567890",
            category.toString(),
            null,
            liveStatsLink,
            hardwareId,
            teamId,
            isCaptain
        );
    }
}
