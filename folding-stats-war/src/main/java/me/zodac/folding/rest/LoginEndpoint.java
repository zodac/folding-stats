package me.zodac.folding.rest;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.db.AuthenticationResponse;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.utils.EncodingUtils;
import me.zodac.folding.ejb.BusinessLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static me.zodac.folding.rest.response.Responses.forbidden;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;
import static me.zodac.folding.rest.response.Responses.unauthorized;

@Path("/login/")
@RequestScoped
public class LoginEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginEndpoint.class);

    @Context
    private UriInfo uriContext;

    @EJB
    private BusinessLogic businessLogic;

    @POST
    @PermitAll
    @Path("/admin/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginAsAdmin(@HeaderParam("Authorization") final String authorizationPayload) {
        LOGGER.debug("Login request received");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Map<String, String> decodedUserNameAndPassword = EncodingUtils.decodeBasicAuthentication(authorizationPayload);
            final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
            final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);
            LOGGER.info("Login request received for user: '{}'", userName);

            final AuthenticationResponse authenticationResponse = businessLogic.isValidUser(userName, password);

            if (!authenticationResponse.isUserExists() || !authenticationResponse.isPasswordMatch()) {
                LOGGER.warn("Invalid user credentials supplied");
                return unauthorized();
            }

            if (!authenticationResponse.isAdmin()) {
                LOGGER.warn("User '{}' is not an admin", userName);
                return forbidden();
            }

            LOGGER.info("Admin user '{}' logged in", userName);
            return ok();
        } catch (final FoldingException e) {
            LOGGER.error("Error validating user credentials", e.getCause());
            return serverError();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error validating user credentials", e);
            return serverError();
        }
    }
}
