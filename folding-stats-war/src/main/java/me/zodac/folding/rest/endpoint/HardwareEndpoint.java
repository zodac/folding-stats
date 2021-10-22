package me.zodac.folding.rest.endpoint;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.validator.HardwareValidator;
import me.zodac.folding.rest.validator.ValidationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for <code>Team Competition</code> {@link Hardware}s.
 *
 * @see me.zodac.folding.client.java.request.HardwareRequestSender
 * @see me.zodac.folding.client.java.response.HardwareResponseParser
 */
@Path("/hardware/")
@RequestScoped
public class HardwareEndpoint extends AbstractCrudEndpoint<HardwareRequest, Hardware> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * {@link POST} request to create a {@link Hardware} based on the input request.
     *
     * @param hardwareRequest the {@link HardwareRequest} to create a {@link Hardware}
     * @return {@link Response.Status#CREATED} containing the created {@link Hardware}
     */
    @Override
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final HardwareRequest hardwareRequest) {
        return super.create(hardwareRequest);
    }

    /**
     * {@link POST} request to create a {@link Collection} of {@link Hardware}s based on the input requests.
     *
     * <p>
     * Will perform a best-effort attempt to create all {@link Hardware}s and will return a response with successful and unsuccessful results.
     *
     * @param hardwareRequests the {@link HardwareRequest}s to create {@link Hardware}s
     * @return {@link Response.Status#OK} containing the created/failed {@link Hardware}s
     */
    @Override
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOf(final Collection<HardwareRequest> hardwareRequests) {
        return super.createBatchOf(hardwareRequests);
    }

    /**
     * {@link GET} request to retrieve all {@link Hardware}s.
     *
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Hardware}s
     */
    @Override
    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context final Request request) {
        return super.getAll(request);
    }

    /**
     * {@link GET} request to retrieve a {@link Hardware}s.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @param request    the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Hardware}
     */
    @Override
    @GET
    @ReadRequired
    @PermitAll
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("hardwareId") final String hardwareId, @Context final Request request) {
        return super.getById(hardwareId, request);
    }

    /**
     * {@link PUT} request to update an existing {@link Hardware} based on the input request.
     *
     * @param hardwareId      the ID of the {@link Hardware} to be updated
     * @param hardwareRequest the {@link HardwareRequest} to update a {@link Hardware}
     * @return {@link Response.Status#OK} containing the updated {@link Hardware}
     */
    @Override
    @PUT
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{hardwareId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@PathParam("hardwareId") final String hardwareId, final HardwareRequest hardwareRequest) {
        return super.updateById(hardwareId, hardwareRequest);
    }

    /**
     * {@link DELETE} request to delete an existing {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be deleted
     * @return {@link Response.Status#OK}
     */
    @Override
    @DELETE
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("hardwareId") final String hardwareId) {
        return super.deleteById(hardwareId);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String elementType() {
        return "hardware";
    }

    @Override
    protected Hardware createElement(final Hardware hardware) {
        return businessLogic.createHardware(hardware);
    }

    @Override
    protected Collection<Hardware> getAllElements() {
        return businessLogic.getAllHardware();
    }

    @Override
    protected ValidationResult<Hardware> validateCreateAndConvert(final HardwareRequest hardwareRequest) {
        final HardwareValidator hardwareValidator = HardwareValidator.create(businessLogic);
        return hardwareValidator.validateCreate(hardwareRequest);
    }

    @Override
    protected ValidationResult<Hardware> validateUpdateAndConvert(final HardwareRequest hardwareRequest, final Hardware existingHardware) {
        final HardwareValidator hardwareValidator = HardwareValidator.create(businessLogic);
        return hardwareValidator.validateUpdate(hardwareRequest, existingHardware);
    }

    @Override
    protected ValidationResult<Hardware> validateDeleteAndConvert(final Hardware hardware) {
        final HardwareValidator hardwareValidator = HardwareValidator.create(businessLogic);
        return hardwareValidator.validateDelete(hardware);
    }

    @Override
    protected Optional<Hardware> getElementById(final int hardwareId) {
        return businessLogic.getHardware(hardwareId);
    }

    @Override
    protected Hardware updateElementById(final Hardware hardware, final Hardware existingHardware) {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Hardware hardwareWithId = Hardware.updateWithId(existingHardware.getId(), hardware);
        return businessLogic.updateHardware(hardwareWithId, existingHardware);
    }

    @Override
    protected void deleteElement(final Hardware hardware) {
        businessLogic.deleteHardware(hardware);
    }
}
