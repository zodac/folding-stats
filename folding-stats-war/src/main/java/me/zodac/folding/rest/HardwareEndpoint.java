package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.validator.HardwareValidator;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * REST endpoints for hardware for <code>folding-stats</code>.
 */
// TODO: [zodac] Add GET query endpoint to retrieve all hardware instances with same name
@Path("/hardware/")
@RequestScoped
public class HardwareEndpoint extends AbstractIdentifiableCrudEndpoint<Hardware> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HardwareEndpoint.class);

    @EJB
    private StorageFacade storageFacade;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHardware(final Hardware hardware) {
        return super.create(hardware);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllHardware() {
        return super.getAll();
    }

    @GET
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwareById(@PathParam("hardwareId") final String hardwareId) {
        return super.getById(hardwareId);
    }

    @PUT
    @Path("/{hardwareId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFoldingTeamById(@PathParam("hardwareId") final String hardwareId, final Hardware hardware) {
        return super.updateById(hardwareId, hardware);
    }

    @DELETE
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
    protected ValidationResponse validate(final Hardware element) {
        return HardwareValidator.isValid(element);
    }

    @Override
    protected Hardware createElement(final Hardware element) throws FoldingException {
        return storageFacade.createHardware(element);
    }

    @Override
    protected List<Hardware> getAllElements() throws FoldingException {
        return storageFacade.getAllHardware();
    }

    @Override
    protected Hardware getElementById(final int elementId) throws FoldingException, NotFoundException {
        return storageFacade.getHardware(elementId);
    }

    @Override
    protected void updateElementById(final Hardware element) throws FoldingException, NotFoundException {
        storageFacade.updateHardware(element);
    }

    @Override
    protected void deleteElementById(final int elementId) throws FoldingConflictException, FoldingException {
        storageFacade.deleteHardware(elementId);
    }
}
