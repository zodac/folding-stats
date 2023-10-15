/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.rest.stub;

import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.api.tc.lars.LarsGpuResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stubbed endpoint for the LARS PPD DB. Used to retrieve metadata about a piece of hardware, rather than going to the real API.
 *
 * <p>
 * We expose additional endpoints to allow the tests to provide {@link LarsGpu}s to the system.
 *
 * @see <a href="https://folding.lar.systems/">LARS PPD DB</a>
 */
@Hidden
@SuppressWarnings("ClassOnlyUsedInOneModule")
@ConditionalOnProperty(name = "stubbed.endpoints.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/gpu_ppd")
public class StubbedLarsEndpoint {

    private static LarsGpuResponse larsGpuResponse = new LarsGpuResponse(new ArrayList<>());

    /**
     * {@link GetMapping} request that returns the configured hardware in the same format as the LARS DB HTML output.
     *
     * <p>
     * Shadows the existing {@code /api/gpu_ppd/gpu_rank_list.json} GET endpoint for retrieving the GPU PPD DB.
     *
     * @return the {@link LarsGpuResponse} response
     */
    @GetMapping(path = "/gpu_rank_list.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LarsGpuResponse> getGpus() {
        return ok(larsGpuResponse);
    }

    /**
     * {@link PostMapping} request that adds a single {@link LarsGpu} to be added to the stubbed GPU PPD DB.
     *
     * <p>
     * This does not shadow an existing endpoint. Since in the test environment we do not want to maintain an actual DB for multiple GPUs, we can
     * manually create and add them here.
     *
     * @param larsGpu the {@link LarsGpu} to add
     * @return {@link HttpStatus#CREATED} {@link ResponseEntity}
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LarsGpu> addGpu(@RequestBody final LarsGpu larsGpu) {
        final Map<String, LarsGpu> existingLarsGpus = new HashMap<>();
        for (final LarsGpu existingGpu : larsGpuResponse.rankedGpus()) {
            existingLarsGpus.put(existingGpu.detailedName(), existingGpu);
        }

        existingLarsGpus.put(larsGpu.detailedName(), larsGpu);

        setLarsResponse(existingLarsGpus.values());
        return created(larsGpu);
    }

    /**
     * {@link DeleteMapping} request to remove all {@link LarsGpu}s from the stubbed endpoint.
     *
     * <p>
     * This does not shadow an existing endpoint. Used for the test environment to clean up all user units.
     *
     * @return {@link HttpStatus#OK} {@link ResponseEntity}
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public ResponseEntity<Void> deleteGpus() {
        setLarsResponse(new ArrayList<>());
        return ok();
    }

    private static void setLarsResponse(final Collection<LarsGpu> larsGpus) {
        larsGpuResponse = new LarsGpuResponse(larsGpus);
    }
}
