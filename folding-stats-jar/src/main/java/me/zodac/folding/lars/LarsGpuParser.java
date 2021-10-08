package me.zodac.folding.lars;

import me.zodac.folding.api.exception.HtmlParseException;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utility class to parse the raw HTML from LARS for a GPU.
 */
final class LarsGpuParser {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int EXPECTED_SPAN_ELEMENTS = 3;
    private static final int EXPECTED_TD_ELEMENTS = 7;

    private LarsGpuParser() {

    }

    /**
     * Parses a GPU {@link Element} retrieved from LARS and converts it into a {@link LarsGpu}.
     *
     * @param gpuEntry the LARS HTML data for a single GPU to parse
     * @return the parsed {@link LarsGpu}s
     * @throws HtmlParseException thrown if an error occurs parsing the input {@link Element}
     */
    static LarsGpu parseSingleGpuEntry(final Element gpuEntry) throws HtmlParseException {
        LOGGER.debug("Parsing GPU entry '{}'", gpuEntry);

        final String displayName = parseDisplayName(gpuEntry);
        final String manufacturer = parseManufacturer(gpuEntry, displayName);
        final String modelInfo = parseModelInfo(gpuEntry, displayName);
        final int rank = parseRank(gpuEntry, displayName);
        final long averagePpd = parseAveragePpd(gpuEntry, displayName);

        return LarsGpu.create(displayName, manufacturer, modelInfo, rank, averagePpd);
    }

    private static String parseDisplayName(final Element gpuEntry) throws HtmlParseException {
        final Elements modelNames = gpuEntry.getElementsByClass("model-name");
        if (modelNames.isEmpty()) {
            throw new HtmlParseException(String.format("Expected at least 1 'model-name' elements, found none for GPU entry: %s", gpuEntry));
        }

        final String modelName = modelNames.get(0).ownText();
        if (modelName.isEmpty()) {
            throw new HtmlParseException(String.format("Empty 'model-name' for GPU entry: %s", gpuEntry));
        }

        return modelName;
    }

    private static String parseManufacturer(final Element gpuEntry, final String displayName) throws HtmlParseException {
        final Elements spans = gpuEntry.getElementsByTag("span");

        if (spans.size() != EXPECTED_SPAN_ELEMENTS) {
            throw new HtmlParseException(String.format("Expected %d 'span' elements for manufacturer, found %d for GPU '%s'", EXPECTED_SPAN_ELEMENTS,
                spans.size(), displayName));
        }

        final String manufacturer = spans.get(2).ownText();
        if (manufacturer.isEmpty()) {
            throw new HtmlParseException(String.format("No manufacturer for GPU '%s'", displayName));
        }

        return manufacturer;
    }

    private static String parseModelInfo(final Element gpuEntry, final String displayName) throws HtmlParseException {
        final Elements modelInfos = gpuEntry.getElementsByClass("model-info");
        if (modelInfos.isEmpty()) {
            throw new HtmlParseException(String.format("Expected at least 1 'model-info' elements, found none for GPU '%s'", displayName));
        }

        final String modelInfo = modelInfos.get(0).ownText();
        if (modelInfo.isEmpty()) {
            throw new HtmlParseException(String.format("Empty 'model-info' for GPU '%s'", displayName));
        }

        return modelInfo;
    }

    private static int parseRank(final Element gpuEntry, final String displayName) throws HtmlParseException {
        final Element rankElement = gpuEntry.getElementsByClass("rank-num").first();
        if (rankElement == null) {
            throw new HtmlParseException(String.format("No 'rank-num' class element found for GPU '%s'", displayName));
        }

        try {
            return Integer.parseInt(rankElement.ownText());
        } catch (final NumberFormatException e) {
            throw new HtmlParseException(String.format("Unable to convert rank into an integer for GPU '%s'", displayName), e);
        }
    }

    private static long parseAveragePpd(final Element gpuEntry, final String displayName) throws HtmlParseException {
        final Elements tableEntries = gpuEntry.getElementsByTag("td");
        if (tableEntries.size() < EXPECTED_TD_ELEMENTS) {
            throw new HtmlParseException(String.format("Expected at least %d 'td' elements, found %d for GPU '%s'", EXPECTED_TD_ELEMENTS,
                tableEntries.size(), displayName));
        }

        final String averagePpdWithCommas = tableEntries.get(2).ownText();
        final String averagePpdWithoutCommas = averagePpdWithCommas.replace(",", "");

        try {
            return Long.parseLong(averagePpdWithoutCommas);
        } catch (final NumberFormatException e) {
            throw new HtmlParseException(String.format("Unable to convert averagePpd into a long for GPU '%s'", displayName), e);
        }
    }
}
