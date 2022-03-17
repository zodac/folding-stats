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

import me.zodac.folding.api.exception.LarsParseException;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utility class to parse the raw HTML from LARS for a GPU.
 */
final class LarsGpuParser {

    private static final Logger LARS_LOGGER = LogManager.getLogger("lars");
    private static final int EXPECTED_SPAN_ELEMENTS = 3;
    private static final int EXPECTED_TD_ELEMENTS = 7;
    private static final int INVALID_RANK = 0;
    private static final long INVALID_AVERAGE_PPD = 0L;

    private LarsGpuParser() {

    }

    /**
     * Parses a GPU {@link Element} retrieved from LARS and converts it into a {@link LarsGpu}.
     *
     * @param gpuEntry the LARS HTML data for a single GPU to parse
     * @return the parsed {@link LarsGpu}s
     * @throws LarsParseException thrown if an error occurs parsing the input {@link Element}
     */
    static LarsGpu parseSingleGpuEntry(final Element gpuEntry) throws LarsParseException {
        LARS_LOGGER.debug("Parsing GPU entry '{}'", gpuEntry);

        final String displayName = parseDisplayName(gpuEntry);
        final String manufacturer = parseManufacturer(gpuEntry, displayName);
        final String modelInfo = parseModelInfo(gpuEntry, displayName);
        final int rank = parseRank(gpuEntry, displayName);
        final long averagePpd = parseAveragePpd(gpuEntry, displayName);

        return LarsGpu.create(displayName, manufacturer, modelInfo, rank, averagePpd);
    }

    private static String parseDisplayName(final Element gpuEntry) throws LarsParseException {
        final Elements modelNames = gpuEntry.getElementsByClass("model-name");
        if (modelNames.isEmpty()) {
            throw new LarsParseException(String.format("Expected at least 1 'model-name' elements, found none for GPU entry: %s", gpuEntry));
        }

        final String modelName = modelNames.get(0).ownText();
        if (modelName.isEmpty()) {
            throw new LarsParseException(String.format("Empty 'model-name' for GPU entry: %s", gpuEntry));
        }

        return modelName;
    }

    private static String parseManufacturer(final Element gpuEntry, final String displayName) throws LarsParseException {
        final Elements spans = gpuEntry.getElementsByTag("span");

        if (spans.size() != EXPECTED_SPAN_ELEMENTS) {
            throw new LarsParseException(String.format("Expected %d 'span' elements for manufacturer, found %d for GPU '%s'", EXPECTED_SPAN_ELEMENTS,
                spans.size(), displayName));
        }

        final String manufacturer = spans.get(2).ownText();
        if (manufacturer.isEmpty()) {
            throw new LarsParseException(String.format("No manufacturer for GPU '%s'", displayName));
        }

        return manufacturer;
    }

    private static String parseModelInfo(final Element gpuEntry, final String displayName) throws LarsParseException {
        final Elements modelInfos = gpuEntry.getElementsByClass("model-info");
        if (modelInfos.isEmpty()) {
            throw new LarsParseException(String.format("Expected at least 1 'model-info' elements, found none for GPU '%s'", displayName));
        }

        final String modelInfo = modelInfos.get(0).ownText();
        if (modelInfo.isEmpty()) {
            throw new LarsParseException(String.format("Empty 'model-info' for GPU '%s'", displayName));
        }

        return modelInfo;
    }

    private static int parseRank(final Element gpuEntry, final String displayName) throws LarsParseException {
        final Element rankElement = gpuEntry.getElementsByClass("rank-num").first();
        if (rankElement == null) {
            throw new LarsParseException(String.format("No 'rank-num' class element found for GPU '%s'", displayName));
        }

        try {
            final int rank = Integer.parseInt(rankElement.ownText());

            if (rank <= INVALID_RANK) {
                throw new LarsParseException(String.format("Unable to use GPU '%s' as it has a rank of '%s'", displayName, rank));
            }

            return rank;
        } catch (final NumberFormatException e) {
            throw new LarsParseException(String.format("Unable to convert rank into an integer for GPU '%s'", displayName), e);
        }
    }

    private static long parseAveragePpd(final Element gpuEntry, final String displayName) throws LarsParseException {
        final Elements tableEntries = gpuEntry.getElementsByTag("td");
        if (tableEntries.size() < EXPECTED_TD_ELEMENTS) {
            throw new LarsParseException(String.format("Expected at least %d 'td' elements, found %d for GPU '%s'", EXPECTED_TD_ELEMENTS,
                tableEntries.size(), displayName));
        }

        final String averagePpdWithCommas = tableEntries.get(2).ownText();
        final String averagePpdWithoutCommas = averagePpdWithCommas.replace(",", "");

        try {
            final long averagePpd = Long.parseLong(averagePpdWithoutCommas);

            if (averagePpd <= INVALID_AVERAGE_PPD) {
                throw new LarsParseException(String.format("Unable to use GPU '%s' as it has an averagePpd of '%s'", displayName, averagePpd));
            }

            return averagePpd;
        } catch (final NumberFormatException e) {
            throw new LarsParseException(String.format("Unable to convert averagePpd into a long for GPU '%s'", displayName), e);
        }
    }
}
