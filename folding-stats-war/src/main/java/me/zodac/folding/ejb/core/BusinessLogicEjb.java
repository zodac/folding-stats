package me.zodac.folding.ejb.core;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Optional;
import javax.ejb.Singleton;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

/**
 * {@link Singleton} EJB implementation of {@link BusinessLogic}.
 *
 * <p>
 * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on the backend storage and caches.
 * But since some logic is needed for special cases (like retrieving the latest Folding@Home stats for a {@link User} when it is created), we
 * implement that logic here, and delegate any CRUD needs to {@link Storage}.
 */
@Singleton
public class BusinessLogicEjb implements BusinessLogic {

    private static final Storage STORAGE = Storage.getInstance();

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return STORAGE.createHardware(hardware);
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return STORAGE.getHardware(hardwareId);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return STORAGE.getAllHardware();
    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        STORAGE.deleteHardware(hardware.getId());
    }

    @Override
    public Team createTeam(final Team team) {
        return STORAGE.createTeam(team);
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return STORAGE.getTeam(teamId);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return STORAGE.getAllTeams();
    }

    @Override
    public void deleteTeam(final Team team) {
        STORAGE.deleteTeam(team.getId());
    }

    @Override
    public Optional<User> getUserWithPasskey(final int userId) {
        return STORAGE.getUser(userId);
    }

    @Override
    public Optional<User> getUserWithoutPasskey(final int userId) {
        final Optional<User> user = STORAGE.getUser(userId);

        if (user.isEmpty()) {
            return user;
        }

        return Optional.of(User.hidePasskey(user.get()));
    }

    @Override
    public Collection<User> getAllUsersWithPasskeys() {
        return STORAGE.getAllUsers();
    }

    @Override
    public Collection<User> getAllUsersWithoutPasskeys() {
        return STORAGE.getAllUsers()
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
