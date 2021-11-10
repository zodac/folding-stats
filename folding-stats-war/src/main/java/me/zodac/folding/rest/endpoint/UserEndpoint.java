/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.endpoint.util.IdResult;
import me.zodac.folding.rest.endpoint.util.IntegerParser;
import me.zodac.folding.rest.response.BatchCreateResponse;
import me.zodac.folding.rest.validator.UserValidator;
import me.zodac.folding.rest.validator.ValidationFailure;
import me.zodac.folding.rest.validator.ValidationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for <code>Team Competition</code> {@link User}s.
 *
 * @see me.zodac.folding.client.java.request.UserRequestSender
 * @see me.zodac.folding.client.java.response.UserResponseParser
 */
@Path("/users/")
@RequestScoped
public class UserEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private FoldingStatsCore foldingStatsCore;

    /**
     * {@link POST} request to create a {@link User} based on the input request.
     *
     * @param userRequest the {@link UserRequest} to create a {@link User}
     * @return {@link Response.Status#CREATED} containing the created {@link User}
     */
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final UserRequest userRequest) {
        LOGGER.debug("POST request received to create user at '{}' with request: {}", uriContext::getAbsolutePath, () -> userRequest);

        final ValidationResult<User> validationResult = validateCreate(userRequest);
        if (validationResult.isFailure()) {
            return validationResult.getFailureResponse();
        }
        final User validatedUser = validationResult.getOutput();

        try {
            final User elementWithId = foldingStatsCore.createUser(validatedUser);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(elementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Created user with ID {}", elementWithId.getId());
            return created(elementWithId, elementLocationBuilder);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating user: {}", userRequest, e);
            return serverError();
        }
    }

    /**
     * {@link POST} request to create a {@link Collection} of {@link User}s based on the input requests.
     *
     * <p>
     * Will perform a best-effort attempt to create all {@link User}s and will return a response with successful and unsuccessful results.
     *
     * @param userRequests the {@link UserRequest}s to create {@link User}s
     * @return {@link Response.Status#OK} containing the created/failed {@link User}s
     */
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOf(final Collection<UserRequest> userRequests) {
        LOGGER.debug("POST request received to create {} users at '{}' with request: {}", userRequests::size, uriContext::getAbsolutePath,
            () -> userRequests);

        final Collection<User> validUsers = new ArrayList<>(userRequests.size() / 2);
        final Collection<ValidationFailure> failedValidationResponses = new ArrayList<>(userRequests.size() / 2);

        for (final UserRequest userRequest : userRequests) {
            final ValidationResult<User> validationResult = validateCreate(userRequest);

            if (validationResult.isFailure()) {
                LOGGER.error("Found validation error for {}: {}", userRequest, validationResult);
                failedValidationResponses.add(validationResult.getValidationFailure());
            } else {
                validUsers.add(validationResult.getOutput());
            }
        }

        if (validUsers.isEmpty()) {
            LOGGER.error("All users contain validation errors: {}", failedValidationResponses);
            return badRequest(failedValidationResponses);
        }

        final List<Object> successful = new ArrayList<>();
        final List<Object> unsuccessful = new ArrayList<>(failedValidationResponses);

        for (final User validUser : validUsers) {
            try {
                final User userWithId = foldingStatsCore.createUser(validUser);
                successful.add(userWithId);
            } catch (final Exception e) {
                LOGGER.error("Unexpected error creating user: {}", validUser, e);
                unsuccessful.add(validUser);
            }
        }

        final BatchCreateResponse batchCreateResponse = BatchCreateResponse.create(successful, unsuccessful);

        if (successful.isEmpty()) {
            return badRequest(batchCreateResponse);
        }

        if (!unsuccessful.isEmpty()) {
            LOGGER.error("{} users successfully created, {} users unsuccessful", successful.size(), unsuccessful.size());
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(batchCreateResponse);
        }

        LOGGER.info("{} users successfully created", successful.size());
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok(batchCreateResponse.getSuccessful());
    }

    /**
     * {@link GET} request to retrieve all {@link User}s.
     *
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}s
     */
    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context final Request request) {
        LOGGER.debug("GET request received for all users at '{}'", uriContext::getAbsolutePath);

        try {
            final Collection<User> elements = foldingStatsCore.getAllUsersWithoutPasskeys();
            return cachedOk(elements, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all users", e);
            return serverError();
        }
    }

    /**
     * {@link GET} request to retrieve a {@link User}.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("userId") final String userId, @Context final Request request) {
        LOGGER.debug("GET request for user received at '{}'", uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingStatsCore.getUserWithoutPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user found with ID {}", userId);
                return notFound();
            }

            final User element = optionalElement.get();
            return cachedOk(element, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    /**
     * {@link PUT} request to update an existing {@link User} based on the input request.
     *
     * @param userId      the ID of the {@link User} to be updated
     * @param userRequest the {@link UserRequest} to update a {@link User}
     * @return {@link Response.Status#OK} containing the updated {@link User}
     */
    @PUT
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@PathParam("userId") final String userId, final UserRequest userRequest) {
        LOGGER.debug("PUT request for user received at '{}'", uriContext::getAbsolutePath);

        if (userRequest == null) {
            LOGGER.error("No payload provided");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingStatsCore.getUserWithPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user found with ID {}", userId);
                return notFound();
            }
            final User existingUser = optionalElement.get();

            if (existingUser.isEqualRequest(userRequest)) {
                LOGGER.debug("No change necessary");
                final User userWithHiddenPasskey = User.hidePasskey(existingUser);
                return ok(userWithHiddenPasskey);
            }

            final ValidationResult<User> validationResult = validateUpdate(userRequest, existingUser);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final User validatedUser = validationResult.getOutput();

            // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
            final User userWithId = User.updateWithId(existingUser.getId(), validatedUser);
            final User updatedUserWithId = foldingStatsCore.updateUser(userWithId, existingUser);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(updatedUserWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Updated user with ID {}", updatedUserWithId.getId());
            return ok(updatedUserWithId, elementLocationBuilder);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
    }

    /**
     * {@link DELETE} request to delete an existing {@link User}.
     *
     * @param userId the ID of the {@link User} to be deleted
     * @return {@link Response.Status#OK}
     */
    @DELETE
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("userId") final String userId) {
        LOGGER.debug("DELETE request for user received at '{}'", uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingStatsCore.getUserWithoutPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user found with ID {}", userId);
                return notFound();
            }
            final User user = optionalElement.get();

            foldingStatsCore.deleteUser(user);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Deleted user with ID {}", userId);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error deleting user with ID: {}", userId, e);
            return serverError();
        }
    }

    private ValidationResult<User> validateCreate(final UserRequest userRequest) {
        final UserValidator userValidator = UserValidator.create();
        return userValidator.validateCreate(
            userRequest,
            foldingStatsCore.getAllUsersWithPasskeys(),
            foldingStatsCore.getAllHardware(),
            foldingStatsCore.getAllTeams()
        );
    }

    private ValidationResult<User> validateUpdate(final UserRequest userRequest, final User existingUser) {
        final UserValidator userValidator = UserValidator.create();
        return userValidator.validateUpdate(
            userRequest,
            existingUser,
            foldingStatsCore.getAllUsersWithPasskeys(),
            foldingStatsCore.getAllHardware(),
            foldingStatsCore.getAllTeams()
        );
    }
}
