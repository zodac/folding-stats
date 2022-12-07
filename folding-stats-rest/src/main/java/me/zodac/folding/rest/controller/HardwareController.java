/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.rest.controller;

import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.validation.HardwareValidator;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.controller.api.HardwareEndpoint;
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
 * Implementation of {@link HardwareEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/hardware")
// TODO: Extract all entry logging to decorator?
public class HardwareController implements HardwareEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

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
    public HardwareController(final HardwareValidator hardwareValidator, final FoldingRepository foldingRepository, final MeterRegistry registry) {
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

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> create(@RequestBody final HardwareRequest hardwareRequest, final HttpServletRequest request) {
        AUDIT_LOGGER.info("POST request received to create hardware at '{}' with request: {}", request::getRequestURI, () -> hardwareRequest);

        final Hardware validatedHardware = hardwareValidator.create(hardwareRequest);
        final Hardware elementWithId = foldingRepository.createHardware(validatedHardware);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Created hardware with ID {}", elementWithId.id());
        hardwareCreates.increment();
        return created(elementWithId, elementWithId.id());
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Hardware>> getAll(final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received for all hardwares at '{}'", request::getRequestURI);
        final Collection<Hardware> elements = foldingRepository.getAllHardware();
        return cachedOk(elements);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> getById(@PathVariable("hardwareId") final int hardwareId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for hardware received at '{}'", request::getRequestURI);

        final Hardware element = foldingRepository.getHardware(hardwareId);
        return cachedOk(element);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> getByHardwareName(@RequestParam("hardwareName") final String hardwareName, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for hardware received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

        final Hardware retrievedHardware = foldingRepository.getAllHardware()
            .stream()
            .filter(hardware -> hardware.hardwareName().equalsIgnoreCase(hardwareName))
            .findAny()
            .orElseThrow(() -> new NotFoundException(Hardware.class, hardwareName));

        return cachedOk(retrievedHardware);
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{hardwareId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> updateById(@PathVariable("hardwareId") final int hardwareId,
                                               @RequestBody final HardwareRequest hardwareRequest,
                                               final HttpServletRequest request
    ) {
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

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("hardwareId") final int hardwareId, final HttpServletRequest request) {
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
