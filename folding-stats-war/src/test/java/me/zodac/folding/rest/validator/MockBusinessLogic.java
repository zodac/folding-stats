package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

/**
 * Mock implementation of {@link BusinessLogic}. Has methods to set specific values for tests.
 */
class MockBusinessLogic implements BusinessLogic {

    private final Map<Integer, Hardware> hardwares = new HashMap<>();
    private final Map<Integer, Team> teams = new HashMap<>();
    private final Map<Integer, User> users = new HashMap<>();

    private MockBusinessLogic() {

    }

    /**
     * Create an instance of {@link MockBusinessLogic}.
     *
     * @return the created {@link MockBusinessLogic} instance
     */
    static MockBusinessLogic create() {
        return new MockBusinessLogic();
    }

    @Override
    public Hardware createHardware(final Hardware hardware) {
        hardwares.put(hardware.getId(), hardware);
        return hardware;
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return Optional.ofNullable(hardwares.get(hardwareId));
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return hardwares.values();
    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        hardwares.remove(hardware.getId());
    }

    @Override
    public Team createTeam(final Team team) {
        teams.put(team.getId(), team);
        return team;
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return Optional.ofNullable(teams.get(teamId));
    }

    @Override
    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    @Override
    public void deleteTeam(final Team team) {
        teams.remove(team.getId());
    }

    public void createUser(final User user) {
        users.put(user.getId(), user);
    }

    public void deleteUser(final User user) {
        users.remove(user.getId());
    }

    @Override
    public Optional<User> getUserWithPasskey(final int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> getUserWithoutPasskey(final int userId) {
        final Optional<User> user = getUserWithPasskey(userId);

        if (user.isEmpty()) {
            return user;
        }

        return Optional.of(User.hidePasskey(user.get()));
    }

    @Override
    public Collection<User> getAllUsersWithPasskeys() {
        return users.values();
    }

    @Override
    public Collection<User> getAllUsersWithoutPasskeys() {
        return getAllUsersWithPasskeys()
            .stream()
            .map(User::hidePasskey)
            .collect(toList());
    }

    @Override
    public Optional<Hardware> getHardwareWithName(final String hardwareName) {
        return getAllHardware()
            .stream()
            .filter(hardware -> hardware.getHardwareName().equalsIgnoreCase(hardwareName))
            .findAny();
    }

    @Override
    public Optional<Team> getTeamWithName(final String teamName) {
        return getAllTeams()
            .stream()
            .filter(team -> team.getTeamName().equalsIgnoreCase(teamName))
            .findAny();
    }

    @Override
    public Collection<User> getUsersWithHardware(final Hardware hardware) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getHardware().getId() == hardware.getId())
            .collect(toList());
    }

    @Override
    public Collection<User> getUsersOnTeam(final Team team) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .collect(toList());
    }

    @Override
    public Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getFoldingUserName().equalsIgnoreCase(foldingUserName) && user.getPasskey().equalsIgnoreCase(passkey))
            .findAny();
    }
}
