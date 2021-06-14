package me.zodac.folding.ejb.core;

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

    // Simple CRUD

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

    // Complex CRUD

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
}
