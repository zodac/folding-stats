package me.zodac.folding.rest;

import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.rest.validator.HardwareValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Collection;

/**
 * REST endpoints for hardware for <code>folding-stats</code>.
 */
// TODO: [zodac] Add GET query endpoint to retrieve all hardware instances with same name
@Path("/hardware/")
@RequestScoped
public class HardwareEndpoint extends AbstractIdentifiableCrudEndpoint<Hardware> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HardwareEndpoint.class);

    @EJB
    private BusinessLogic businessLogic;


    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHardware(final Hardware hardware) {
        return super.create(hardware);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfHardware(final Collection<Hardware> hardware) {
        return super.createBatchOf(hardware);
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
    public Response updateFoldingTeamById(@PathParam("hardwareId") final String hardwareId, final Hardware hardware) {
        return super.updateById(hardwareId, hardware);
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
    protected ValidationResponse validate(final Hardware hardware) {
        final HardwareValidator hardwareValidator = HardwareValidator.create();
        return hardwareValidator.isValid(hardware);
    }

    @Override
    protected Hardware createElement(final Hardware hardware) throws FoldingException, FoldingConflictException {
        return businessLogic.createHardware(hardware);
    }

    @Override
    protected Collection<Hardware> getAllElements() throws FoldingException {
        return businessLogic.getAllHardware();
    }

    @Override
    protected Hardware getElementById(final int hardwareId) throws FoldingException, NotFoundException {
        return businessLogic.getHardware(hardwareId);
    }

    @Override
    protected Hardware updateElementById(final int hardwareId, final Hardware hardware) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException {
        if (hardware.getId() == 0) {
            // The payload 'should' have the ID, but it's not necessary if the correct URL is used
            final Hardware hardwareWithId = Hardware.updateWithId(hardwareId, hardware);
            businessLogic.updateHardware(hardwareWithId);
            return hardwareWithId;
        }

        businessLogic.updateHardware(hardware);
        return hardware;
    }

    @Override
    protected void deleteElementById(final int hardwareId) throws FoldingConflictException, FoldingException {
        businessLogic.deleteHardware(hardwareId);
    }
}
