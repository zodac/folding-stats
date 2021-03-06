/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.rest;

import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collection;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.validation.HardwareValidator;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for {@code Team Competition} {@link Hardware}s.
 */
@RestController
@RequestMapping("/hardware")
public class HardwareEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger("audit");

    private final HardwareValidator hardwareValidator;
    private final FoldingRepository foldingRepository;

    // Prometheus counters
    private final Counter hardwareCreates;
    private final Counter hardwareUpdates;
    private final Counter hardwareDeletes;

    /**
     * {@link Autowired} constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param hardwareValidator the {@link HardwareValidator}
     * @param foldingRepository the {@link FoldingRepository}
     * @param registry          the Prometheus {@link MeterRegistry}
     */
    @Autowired
    public HardwareEndpoint(final HardwareValidator hardwareValidator, final FoldingRepository foldingRepository, final MeterRegistry registry) {
        this.hardwareValidator = hardwareValidator;
        this.foldingRepository = foldingRepository;

        hardwareCreates = Counter.builder("hardware_create_counter")
            .description("Number of Hardware creations through the REST endpoint")
            .register(registry);
        hardwareUpdates = Counter.builder("hardware_update_counter")
            .description("Number of Hardware updates through the REST endpoint")
            .register(registry);
        hardwareDeletes = Counter.builder("hardware_delete_counter")
            .description("Number of Hardware deletions through the REST endpoint")
            .register(registry);
    }

    /**
     * {@link PostMapping} request to create a {@link Hardware} based on the input request.
     *
     * @param hardwareRequest the {@link HardwareRequest} to create a {@link Hardware}
     * @param request         the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link Hardware}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody final HardwareRequest hardwareRequest, final HttpServletRequest request) {
        AUDIT_LOGGER.info("POST request received to create hardware at '{}' with request: {}", request::getRequestURI, () -> hardwareRequest);

        final Hardware validatedHardware = hardwareValidator.create(hardwareRequest);
        final Hardware elementWithId = foldingRepository.createHardware(validatedHardware);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Created hardware with ID {}", elementWithId.id());
        hardwareCreates.increment();
        return created(elementWithId, elementWithId.id());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link Hardware}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Hardware}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received for all hardwares at '{}'", request::getRequestURI);
        final Collection<Hardware> elements = foldingRepository.getAllHardware();
        return cachedOk(elements);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @param request    the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Hardware}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("hardwareId") final int hardwareId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for hardware received at '{}'", request::getRequestURI);

        final Hardware element = foldingRepository.getHardware(hardwareId);
        return cachedOk(element);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareName}.
     *
     * @param hardwareName the {@code hardwareName} of the {@link Hardware} to retrieve
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Hardware}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByHardwareName(@RequestParam("hardwareName") final String hardwareName, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for hardware received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

        final Hardware retrievedHardware = foldingRepository.getAllHardware()
            .stream()
            .filter(hardware -> hardware.hardwareName().equalsIgnoreCase(hardwareName))
            .findAny()
            .orElseThrow(() -> new NotFoundException(Hardware.class, hardwareName));

        return cachedOk(retrievedHardware);
    }

    /**
     * {@link PutMapping} request to update an existing {@link Hardware} based on the input request.
     *
     * @param hardwareId      the ID of the {@link Hardware} to be updated
     * @param hardwareRequest the {@link HardwareRequest} to update a {@link Hardware}
     * @param request         the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link Hardware}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{hardwareId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateById(@PathVariable("hardwareId") final int hardwareId,
                                        @RequestBody final HardwareRequest hardwareRequest,
                                        final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request for hardware received at '{}' with request {}", request::getRequestURI, () -> hardwareRequest);

        final Hardware existingHardware = foldingRepository.getHardware(hardwareId);

        if (existingHardware.isEqualRequest(hardwareRequest)) {
            AUDIT_LOGGER.info("Request is same as existing hardware");
            return ok(existingHardware);
        }

        final Hardware validatedHardware = hardwareValidator.update(hardwareRequest, existingHardware);

        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Hardware hardwareWithId = Hardware.updateWithId(existingHardware.id(), validatedHardware);
        final Hardware updatedHardwareWithId = foldingRepository.updateHardware(hardwareWithId, existingHardware);

        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Updated hardware with ID {}", updatedHardwareWithId.id());
        hardwareUpdates.increment();
        return ok(updatedHardwareWithId, updatedHardwareWithId.id());
    }

    /**
     * {@link DeleteMapping} request to delete an existing {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be deleted
     * @param request    the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteById(@PathVariable("hardwareId") final int hardwareId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("DELETE request for hardware received at '{}'", request::getRequestURI);

        final Hardware hardware = foldingRepository.getHardware(hardwareId);
        final Hardware validatedHardware = hardwareValidator.delete(hardware);

        foldingRepository.deleteHardware(validatedHardware);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Deleted hardware with ID {}", hardwareId);
        hardwareDeletes.increment();
        return ok();
    }
}