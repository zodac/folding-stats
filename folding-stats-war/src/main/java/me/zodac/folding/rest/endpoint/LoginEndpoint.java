package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.forbidden;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;
import static me.zodac.folding.rest.response.Responses.unauthorized;

import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemUserAuthentication;
import me.zodac.folding.api.utils.EncodingUtils;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.rest.api.LoginCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/login/")
@RequestScoped
public class LoginEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private OldFacade oldFacade;

    @POST
    @PermitAll
    @Path("/admin/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginAsAdmin(final LoginCredentials loginCredentials) {
        LOGGER.debug("Login request received");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        if (loginCredentials == null || EncodingUtils.isNotBasicAuthentication(loginCredentials.getEncodedUserNameAndPassword())) {
            LOGGER.error("Invalid payload: {}", loginCredentials);
            return badRequest(loginCredentials);
        }

        try {
            final Map<String, String> decodedUserNameAndPassword =
                EncodingUtils.decodeBasicAuthentication(loginCredentials.getEncodedUserNameAndPassword());
            final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
            final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);
            LOGGER.debug("Login request received for user: '{}'", userName);

            final SystemUserAuthentication systemUserAuthentication = oldFacade.authenticateSystemUser(userName, password);

            if (!systemUserAuthentication.isUserExists() || !systemUserAuthentication.isPasswordMatch()) {
                LOGGER.warn("Invalid user credentials supplied: {}", loginCredentials);
                return unauthorized();
            }

            if (!systemUserAuthentication.isAdmin()) {
                LOGGER.warn("User '{}' is not an admin", userName);
                return forbidden();
            }

            LOGGER.info("Admin user '{}' logged in", userName);
            return ok();
        } catch (final IllegalArgumentException e) {
            LOGGER.error("Encoded user name and password was not a valid Base64 string", e);
            return badRequest(loginCredentials);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error validating user credentials", e);
            return serverError();
        }
    }
}