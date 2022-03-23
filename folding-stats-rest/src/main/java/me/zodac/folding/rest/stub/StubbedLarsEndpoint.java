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

package me.zodac.folding.rest.stub;

import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;

import java.util.ArrayList;
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
 * Since the LARS DB does not have an API, the production code scrapes the HTML page itself. To correctly test, we will need to reconstruct the HTML
 * structure (at least the parts that the production code expected), then populate our configured hardware in a similar way to the LARS DB itself.
 *
 * <p>
 * We expose additional endpoints to allow the tests to provide {@link LarsGpu}s to the system.
 *
 * @see <a href="https://folding.lar.systems/">LARS PPD DB</a>
 */
@SuppressWarnings("ClassOnlyUsedInOneModule")
@ConditionalOnProperty("stubbed.endpoints.enabled")
@RestController
@RequestMapping("/api/gpu_ppd")
public class StubbedLarsEndpoint {

    private static final LarsGpuResponse LARS_GPU_RESPONSE = createLarsResponse();

    /**
     * {@link PostMapping} request that allows tests to provide a single {@link LarsGpu} to be added to the stubbed response.
     *
     * @param larsGpuInput the {@link LarsGpu} to add, as a {@link String}
     * @return {@link HttpStatus#CREATED} {@link ResponseEntity}
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LarsGpu> addGpu(@RequestBody final String larsGpuInput) {
        final LarsGpu larsGpu = GSON.fromJson(larsGpuInput, LarsGpu.class);

        final Map<String, LarsGpu> existingLarsGpus = new HashMap<>();
        for (final LarsGpu existingGpu : LARS_GPU_RESPONSE.getRankedGpus()) {
            existingLarsGpus.put(existingGpu.getDetailedName(), existingGpu);
        }

        existingLarsGpus.put(larsGpu.getDetailedName(), larsGpu);

        LARS_GPU_RESPONSE.setRankedGpus(existingLarsGpus.values());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(larsGpu);
    }

    /**
     * {@link DeleteMapping} request to remove all {@link LarsGpu}s from the stubbed endpoint.
     *
     * @return {@link HttpStatus#OK} {@link ResponseEntity}
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public ResponseEntity<Void> deleteGpus() {
        LARS_GPU_RESPONSE.setRankedGpus(new ArrayList<>());
        return ResponseEntity
            .ok()
            .build();
    }

    /**
     * {@link GetMapping} request that returns the configured hardware in the same format as the LARS DB HTML output.
     *
     * <p>
     * Used by production code, not by tests directly, though tests should populate the hardware using {@link #addGpu(String)}.
     *
     * @return the HTML output
     * @see StubbedLarsEndpoint#addGpu(String)
     */
    @GetMapping(path = "/gpu_rank_list.json", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getGpus() {
        return ResponseEntity
            .ok()
            .body(GSON.toJson(LARS_GPU_RESPONSE));
    }

    private static LarsGpuResponse createLarsResponse() {
        final LarsGpuResponse larsGpuResponse = new LarsGpuResponse();
        larsGpuResponse.setApiName("");
        larsGpuResponse.setApiDescription("");
        larsGpuResponse.setApiLicence("");
        larsGpuResponse.setCreditLinkWebsite("");
        larsGpuResponse.setCreditLinkChromeExtension("");
        larsGpuResponse.setDateOfLastUpdate("");
        larsGpuResponse.setRankedGpus(new ArrayList<>());

        return larsGpuResponse;
    }
}
