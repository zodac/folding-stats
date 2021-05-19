package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Utility class used to generate {@link Hardware}, {@link User} and {@link Team} instances for test cases.
 */
public final class TestGenerator {

    private static int hardwareCount = 1;
    private static int userCount = 1;
    private static int teamCount = 1;

    private TestGenerator() {

    }


    /**
     * Generate a {@link Hardware} with a {@link Hardware#getMultiplier()} of <b>x1</b>.
     *
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardware() {
        return Hardware.createWithoutId(
                "Dummy_Hardware" + hardwareCount++,
                "Dummy Hardware",
                OperatingSystem.WINDOWS,
                1.0D
        );
    }

    /**
     * Generate a {@link Hardware} with a {@link Hardware#getMultiplier()} of <b>x1</b>.
     *
     * @param hardwareId the ID of the {@link Hardware}
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardwareWithId(final int hardwareId) {
        return Hardware.create(
                hardwareId,
                "Dummy_Hardware" + hardwareCount++,
                "Dummy Hardware",
                OperatingSystem.WINDOWS,
                1.0D
        );
    }

    /**
     * Generate a {@link Hardware}.
     *
     * @param multiplier the multiplier to be applied to the {@link Hardware}
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardwareWithMultiplier(final double multiplier) {
        return Hardware.createWithoutId(
                "Dummy_Hardware" + hardwareCount++,
                "Dummy Hardware",
                OperatingSystem.WINDOWS,
                multiplier
        );
    }

    /**
     * Generate a {@link Hardware}.
     *
     * @param operatingSystem the {@link OperatingSystem} of the {@link Hardware}
     * @return the generated {@link Hardware}
     */
    public static Hardware generateHardwareWithOperatingSystem(final OperatingSystem operatingSystem) {
        return Hardware.createWithoutId(
                "Dummy_Hardware" + hardwareCount++,
                "Dummy Hardware",
                operatingSystem,
                1.0D
        );
    }

    /**
     * Generates a {@link User} linked with an auto-created {@link Hardware}.
     *
     * @return the generated {@link User}
     * @throws FoldingRestException thrown if an error occurs creating the {@link Hardware}
     * @see #generateHardware()
     */
    public static User generateUser() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        return generateUserWithUserId(hardwareId);
    }

    /**
     * Generates a {@link User} with the supplied {@link Hardware} ID.
     *
     * @param hardwareId the ID of the {@link Hardware} for the user
     * @return the generated {@link User}
     */
    public static User generateUserWithHardwareId(final int hardwareId) {
        return User.createWithoutId(
                "Dummy_User" + userCount,
                "Dummy User" + userCount,
                "DummyPasskey" + userCount++,
                Category.NVIDIA_GPU,
                hardwareId,
                "",
                false);
    }

    /**
     * Generates a {@link User} with the supplied {@link Category} linked with an auto-created {@link Hardware}.
     *
     * @param category the {@link Category} of the {@link Hardware} for the user
     * @return the generated {@link User}
     * @throws FoldingRestException thrown if an error occurs creating the {@link Hardware}
     * @see #generateHardware()
     */
    public static User generateUserWithCategory(final Category category) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        return User.createWithoutId(
                "Dummy_User" + userCount,
                "Dummy User" + userCount,
                "DummyPasskey" + userCount++,
                category,
                hardwareId,
                "",
                false);
    }

    /**
     * Generates a {@link User} with the supplied {@link User} ID linked with an auto-created {@link Hardware}.
     *
     * @param userId the ID of the {@link User}
     * @return the generated {@link User}
     * @throws FoldingRestException thrown if an error occurs creating the {@link Hardware}
     * @see #generateHardware()
     */
    public static User generateUserWithUserId(final int userId) throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        return User.create(
                userId,
                "Dummy_User" + userCount,
                "Dummy User" + userCount,
                "DummyPasskey" + userCount++,
                Category.NVIDIA_GPU,
                hardwareId,
                "",
                false);
    }

    /**
     * Generates a {@link Team} with a single {@link User}.
     *
     * @return the generated {@link Team}
     * @throws FoldingRestException thrown if an error occurs creating the {@link User} or {@link Hardware}
     * @see #generateUser()
     */
    public static Team generateTeam() throws FoldingRestException {
        final User user = generateUser();
        final int userId = UserUtils.createOrConflict(user).getId();
        return Team.createWithoutId(
                "Dummy_Team" + teamCount++,
                "Dummy Team",
                userId,
                Set.of(userId),
                Collections.emptySet());
    }

    /**
     * Generates a {@link Team} with a single {@link User}.
     *
     * @param teamId the ID of the {@link Team}
     * @return the generated {@link Team}
     * @throws FoldingRestException thrown if an error occurs creating the {@link User} or {@link Hardware}
     * @see #generateUser()
     */
    public static Team generateTeamWithId(final int teamId) throws FoldingRestException {
        final User user = generateUser();
        final int userId = UserUtils.createOrConflict(user).getId();
        return Team.create(
                teamId,
                "Dummy_Team" + teamCount++,
                "Dummy Team",
                userId,
                Set.of(userId),
                Collections.emptySet());
    }

    /**
     * Generates a {@link Team} with with the supplied {@link User} IDs.
     *
     * @param captainId   the ID of the captain {@link User}
     * @param teamUserIds the IDs of the rest of the {@link Team} {@link User}s
     * @return the generated {@link Team}
     */
    public static Team generateTeamWithUserIds(final int captainId, final int... teamUserIds) {
        final Set<Integer> userIds = new HashSet<>();
        userIds.add(captainId);
        userIds.addAll(Arrays.stream(teamUserIds).boxed().collect(toSet()));

        return Team.createWithoutId(
                "Dummy_Team" + teamCount++,
                "Dummy Team",
                captainId,
                userIds,
                Collections.emptySet());
    }
}
