/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.test.stub;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RestController
@RequestMapping("/gpu_ppd/overall_ranks")
public class StubbedLarsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, LarsGpu> LARS_GPUS_BY_MODEL_INFO = new HashMap<>();

    private static final String LARS_GPU_TEMPLATE = "<tr>\n"
        + "<td class=\"rank-num\">${rank}</td>\n"
        + "<td>\n"
        + "<a title=\"${displayName} folding PPD information: ${modelInfo}\" href=\"/gpu_ppd/brands/${manufacturer}/folding_profile/${modelInfo}\">\n"
        + "<span class=\"text-${manufacturer} model-name\">${displayName}</span> <i class=\"uil-link mr-1\"></i>\n"
        + "</a>\n"
        + "<br />\n"
        + "<span class=\"model-info\">${modelInfo}</span>\n"
        + "</td>\n"
        + "<td>${averagePpd}</td>\n"
        + "<td>linuxAveragePpd_ignored</td>\n"
        + "<td>windowsAveragePpd_ignored</td>\n"
        + "<td>\n"
        + "<a href=\"/gpu_ppd/brands/${manufacturer}\">\n"
        + "<span class=\"text-${manufacturer}\">${manufacturer}</span> <i class=\"uil-link mr-1\"></i>\n"
        + "</a>\n"
        + "</td>\n"
        + "<td class=\"text-uppercase\">model_ignored</td>\n"
        + "</tr>";

    /**
     * <b>POST</b> request that allows tests to provide a single {@link LarsGpu} to be added to the stubbed response.
     *
     * @param larsGpu the {@link LarsGpu} to add
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addGpu(@RequestBody final LarsGpu larsGpu) {
        LOGGER.info("Received LarsGpu: {}", larsGpu);
        LARS_GPUS_BY_MODEL_INFO.put(larsGpu.getModelInfo(), larsGpu);
    }

    /**
     * <b>DELETE</b> request to remove all {@link LarsGpu}s from the stubbed endpoint.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteGpus() {
        LARS_GPUS_BY_MODEL_INFO.clear();
    }

    /**
     * <b>GET</b> request that returns the configured hardware in the same format as the LARS DB HTML output.
     *
     * <p>
     * Used by production code, not by tests directly, though tests should populate the hardware using {@link #addGpu(LarsGpu)}.
     *
     * @return the HTML output
     * @see StubbedLarsEndpoint#addGpu(LarsGpu)
     */
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getGpus() {
        return createGpuPage();
    }

    private static String createGpuPage() {
        if (LARS_GPUS_BY_MODEL_INFO.isEmpty()) {
            return "<html><table id=\"primary-database\"></table></html>";
        }

        final StringBuilder htmlPage = new StringBuilder(68) // The defined String literals have at least 68 characters
            .append("<html><table id=\"primary-datatable\"><tbody>");

        final Collection<String> trElementsForHardwares = createTrElementsForHardwares();
        for (final String trElementForHardware : trElementsForHardwares) {
            htmlPage.append(trElementForHardware);
        }

        return htmlPage
            .append("</tbody></table></html>")
            .toString();
    }

    private static Collection<String> createTrElementsForHardwares() {
        return LARS_GPUS_BY_MODEL_INFO
            .values()
            .stream()
            .map(StubbedLarsEndpoint::createTrElementForHardware)
            .filter(Objects::nonNull)
            .collect(toList());
    }

    private static String createTrElementForHardware(final LarsGpu larsGpu) {
        try {
            final Map<String, String> substitutionValues = Map.of(
                "displayName", larsGpu.getDisplayName(),
                "manufacturer", larsGpu.getManufacturer(),
                "modelInfo", larsGpu.getModelInfo(),
                "rank", String.valueOf(larsGpu.getRank()),
                "averagePpd", String.valueOf(larsGpu.getAveragePpd())
            );

            final StringSubstitutor substitutor = new StringSubstitutor(substitutionValues);
            return substitutor.replace(LARS_GPU_TEMPLATE);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error building element for LARS GPU: {}", larsGpu, e);
            return null;
        }
    }
}
