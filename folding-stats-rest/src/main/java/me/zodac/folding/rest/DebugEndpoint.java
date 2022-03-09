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

import static me.zodac.folding.rest.response.Responses.ok;

import javax.annotation.security.RolesAllowed;
import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.bean.tc.lars.LarsHardwareUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for debugging.
 *
 * <p>
 * <b>NOTE:</b> There are no client-libraries for these endpoints.
 */
@RestController
@RequestMapping("/debug")
public class DebugEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingRepository foldingRepository;
    private final LarsHardwareUpdater larsHardwareUpdater;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository   the {@link FoldingRepository}
     * @param larsHardwareUpdater the {@link LarsHardwareUpdater}
     */
    public DebugEndpoint(final FoldingRepository foldingRepository, final LarsHardwareUpdater larsHardwareUpdater) {
        this.foldingRepository = foldingRepository;
        this.larsHardwareUpdater = larsHardwareUpdater;
    }

    /**
     * {@link PostMapping} request to print the contents of all caches to the system log.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     * @see LarsHardwareUpdater
     */
    @RolesAllowed("admin")
    @PostMapping(path = "/lars", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> startLarsUpdate() {
        LOGGER.info("GET request received to manually update hardware from LARS DB");
        larsHardwareUpdater.retrieveHardwareAndPersist();
        return ok();
    }

    /**
     * {@link PostMapping} request to print the contents of all caches to the system log.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     * @see FoldingRepository#printCacheContents()
     */
    @RolesAllowed("admin")
    @PostMapping(path = "/caches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> printCaches() {
        LOGGER.info("Printing cache contents");
        foldingRepository.printCacheContents();
        return ok();
    }
}