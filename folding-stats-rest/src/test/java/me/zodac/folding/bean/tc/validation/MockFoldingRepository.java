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

package me.zodac.folding.bean.tc.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.exception.NotFoundException;

/**
 * Mock implementation of {@link FoldingRepository} for unit tests.
 */
class MockFoldingRepository implements FoldingRepository {

    private final Map<Integer, Hardware> hardwares = new HashMap<>();
    private final Map<Integer, Team> teams = new HashMap<>();
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, UserChange> userChanges = new HashMap<>();

    @Override
    public Hardware createHardware(final Hardware hardware) {
        hardwares.put(hardware.id(), hardware);
        return hardware;
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return hardwares.values();
    }

    @Override
    public Hardware getHardware(final int hardwareId) {
        return hardwares.computeIfAbsent(hardwareId, v -> {
            throw new NotFoundException(Hardware.class, hardwareId);
        });
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware) {
        hardwares.put(hardwareToUpdate.id(), hardwareToUpdate);
        return hardwareToUpdate;
    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        hardwares.remove(hardware.id());
    }

    @Override
    public Team createTeam(final Team team) {
        teams.put(team.id(), team);
        return team;
    }

    @Override
    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    @Override
    public Team getTeam(final int teamId) {
        return teams.computeIfAbsent(teamId, v -> {
            throw new NotFoundException(Team.class, teamId);
        });
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        teams.put(teamToUpdate.id(), teamToUpdate);
        return teamToUpdate;
    }

    @Override
    public void deleteTeam(final Team team) {
        teams.remove(team.id());
    }

    @Override
    public User createUser(final User user) {
        users.put(user.id(), user);
        return user;
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
            .toList();
    }

    @Override
    public User getUserWithPasskey(final int userId) {
        return users.computeIfAbsent(userId, v -> {
            throw new NotFoundException(User.class, userId);
        });
    }

    @Override
    public User getUserWithoutPasskey(final int userId) {
        final User userWithPasskey = getUserWithPasskey(userId);
        return User.hidePasskey(userWithPasskey);
    }

    @Override
    public User updateUser(final User userToUpdate, final User existingUser) {
        users.put(userToUpdate.id(), userToUpdate);
        return userToUpdate;
    }

    @Override
    public void deleteUser(final User user) {
        users.remove(user.id());
    }

    @Override
    public Collection<User> getUsersOnTeam(final Team team) {
        if (team.id() == Team.EMPTY_TEAM_ID) {
            return List.of();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.team().id() == team.id())
            .map(User::hidePasskey)
            .toList();
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        return UserAuthenticationResult.success(Set.of("admin"));
    }

    @Override
    public UserChange createUserChange(final UserChange userChange) {
        userChanges.put(userChange.id(), userChange);
        return userChange;
    }

    @Override
    public Collection<UserChange> getAllUserChangesWithPasskeys(final Collection<UserChangeState> states, final long numberOfMonths) {
        return userChanges.values()
            .stream()
            .filter(userChange -> states.contains(userChange.state()))
            .toList();
    }

    @Override
    public Collection<UserChange> getAllUserChangesWithoutPasskeys(final Collection<UserChangeState> states, final long numberOfMonths) {
        return userChanges.values()
            .stream()
            .map(UserChange::hidePasskey)
            .toList();
    }

    @Override
    public Collection<UserChange> getAllUserChangesForNextMonth() {
        return userChanges.values()
            .stream()
            .filter(userChange -> userChange.state() == UserChangeState.APPROVED_NEXT_MONTH)
            .toList();
    }

    @Override
    public UserChange getUserChange(final int userChangeId) {
        return userChanges.computeIfAbsent(userChangeId, v -> {
            throw new NotFoundException(UserChange.class, userChangeId);
        });
    }

    @Override
    public UserChange updateUserChange(final UserChange userChangeToUpdate) {
        userChanges.put(userChangeToUpdate.id(), userChangeToUpdate);
        return userChangeToUpdate;
    }

    @Override
    public void printCacheContents() {

    }
}
