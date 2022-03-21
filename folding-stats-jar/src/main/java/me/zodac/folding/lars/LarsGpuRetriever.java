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

package me.zodac.folding.lars;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utility class that retrieves {@link me.zodac.folding.api.tc.HardwareType#GPU} data from the LARS DB.
 */
public final class LarsGpuRetriever {

    private static final Logger LARS_LOGGER = LogManager.getLogger("lars");

    private LarsGpuRetriever() {

    }

    /**
     * Will retrieve the {@link me.zodac.folding.api.tc.HardwareType#GPU} data from the supplied DB URL.
     *
     * <p>
     * If an error occurs retrieving any individual {@link LarsGpu}, it will be logged, and we continue processing the remainder.
     *
     * @param gpuDatabaseUrl the URL to the LARS GPU database
     * @return a {@link Collection} of parsed {@link LarsGpu}s sorted by {@link LarsGpu#getAveragePpd()}
     * @see #retrieveGpus(Document)
     */
    public static List<LarsGpu> retrieveGpus(final String gpuDatabaseUrl) {
        LARS_LOGGER.debug("Retrieving LARS GPU data from: '{}'", gpuDatabaseUrl);

        try {
            final Document doc = Jsoup.connect(gpuDatabaseUrl).get();
            return retrieveGpus(doc);
        } catch (final Exception e) {
            LARS_LOGGER.warn("Error retrieving data from LARS GPU DB", e);
            return Collections.emptyList();
        }
    }

    /**
     * Will retrieve the {@link me.zodac.folding.api.tc.HardwareType#GPU} data from the supplied {@link Document}.
     *
     * <p>
     * If an error occurs retrieving any individual {@link LarsGpu}, it will be logged, and we continue processing the remainder.
     *
     * @param doc the HTML page from the LARS GPU DB
     * @return a {@link Collection} of parsed {@link LarsGpu}s sorted by {@link LarsGpu#getAveragePpd()}
     */
    public static List<LarsGpu> retrieveGpus(final Document doc) {
        final Element databaseTable = doc.getElementById("primary-datatable");

        if (databaseTable == null) {
            LARS_LOGGER.warn("Unable to find the 'primary-database' table");
            return Collections.emptyList();
        }

        final Element databaseBody = databaseTable.getElementsByTag("tbody").first();

        if (databaseBody == null) {
            LARS_LOGGER.warn("Unable to find a 'tbody' entry in the database table");
            return Collections.emptyList();
        }

        final Elements databaseEntries = databaseBody.getElementsByTag("tr");
        if (databaseEntries.isEmpty()) {
            LARS_LOGGER.warn("No 'tr' entries found in the database table");
            return Collections.emptyList();
        }

        return databaseEntries
            .stream()
            .map(LarsGpuRetriever::safeParse)
            .filter(Objects::nonNull)
            .sorted(LarsGpuAveragePpdComparator.create())
            .toList();
    }

    private static LarsGpu safeParse(final Element element) {
        try {
            return LarsGpuParser.parseSingleGpuEntry(element);
        } catch (final LarsParseException e) {
            LARS_LOGGER.debug("Error parsing GPU entry: {}", element, e);
            LARS_LOGGER.warn("Error parsing GPU entry: {}", e.getMessage());
            return null;
        }
    }
}
