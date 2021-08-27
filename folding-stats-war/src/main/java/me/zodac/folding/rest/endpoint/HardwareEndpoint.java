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
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.validator.HardwareValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for {@link Hardware}s for <code>folding-stats</code>.
 */
// TODO: [zodac] Add GET query endpoint to retrieve all hardware instances with same name
@Path("/hardware/")
@RequestScoped
public class HardwareEndpoint extends AbstractCrudEndpoint<HardwareRequest, Hardware> {

    private static final Logger LOGGER = LogManager.getLogger();

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHardware(final HardwareRequest hardwareRequest) {
        return super.create(hardwareRequest);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfHardware(final Collection<HardwareRequest> hardwareRequests) {
        return super.createBatchOf(hardwareRequests);
    }

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllHardware(@Context final Request request) {
        return super.getAll(request);
    }

    @GET
    @PermitAll
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwareById(@PathParam("hardwareId") final String hardwareId, @Context final Request request) {
        return super.getById(hardwareId, request);
    }

    @PUT
    @RolesAllowed("admin")
    @Path("/{hardwareId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFoldingTeamById(@PathParam("hardwareId") final String hardwareId, final HardwareRequest hardwareRequest) {
        return super.updateById(hardwareId, hardwareRequest);
    }

    @DELETE
    @RolesAllowed("admin")
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteHardwareById(@PathParam("hardwareId") final String hardwareId) {
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
    protected ValidationResponse<Hardware> validateCreateAndConvert(final HardwareRequest hardwareRequest) {
        final HardwareValidator hardwareValidator = HardwareValidator.create(businessLogic);
        return hardwareValidator.validateCreate(hardwareRequest);
    }

    @Override
    protected ValidationResponse<Hardware> validateUpdateAndConvert(final HardwareRequest hardwareRequest, final Hardware existingHardware) {
        final HardwareValidator hardwareValidator = HardwareValidator.create(businessLogic);
        return hardwareValidator.validateUpdate(hardwareRequest);
    }

    @Override
    protected ValidationResponse<Hardware> validateDeleteAndConvert(final Hardware hardware) {
        final HardwareValidator hardwareValidator = HardwareValidator.create(businessLogic);
        return hardwareValidator.validateDelete(hardware);
    }

    @Override
    protected Optional<Hardware> getElementById(final int hardwareId) {
        return businessLogic.getHardware(hardwareId);
    }

    @Override
    protected Hardware updateElementById(final int hardwareId, final Hardware hardware, final Hardware existingHardware)
        throws ExternalConnectionException {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Hardware hardwareWithId = Hardware.updateWithId(hardwareId, hardware);
        oldFacade.updateHardware(hardwareWithId, existingHardware);
        return hardwareWithId;
    }

    @Override
    protected void deleteElement(final Hardware hardware) {
        businessLogic.deleteHardware(hardware);
    }
}
