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

import static me.zodac.folding.rest.response.Responses.ok;

import jakarta.annotation.security.RolesAllowed;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.lars.LarsHardwareUpdater;
import me.zodac.folding.rest.controller.api.DebugEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementation of {@link DebugEndpoint} REST endpoints.
 *
 * <p>
 * <b>NOTE:</b> There are no client-libraries for these endpoints.
 */
@RestController
@RequestMapping("/debug")
public class DebugController implements DebugEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final FoldingRepository foldingRepository;
    private final LarsHardwareUpdater larsHardwareUpdater;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository   the {@link FoldingRepository}
     * @param larsHardwareUpdater the {@link LarsHardwareUpdater}
     */
    @Autowired
    public DebugController(final FoldingRepository foldingRepository, final LarsHardwareUpdater larsHardwareUpdater) {
        this.foldingRepository = foldingRepository;
        this.larsHardwareUpdater = larsHardwareUpdater;
    }

    @Override
    @RolesAllowed("admin")
    @PostMapping(path = "/lars", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> startLarsUpdate() {
        AUDIT_LOGGER.info("GET request received to manually update hardware from LARS DB");
        larsHardwareUpdater.retrieveHardwareAndPersist();
        return ok();
    }

    @Override
    @RolesAllowed("admin")
    @PostMapping(path = "/caches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> printCaches() {
        AUDIT_LOGGER.info("Printing cache contents");
        foldingRepository.printCacheContents();
        return ok();
    }
}
