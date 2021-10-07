package me.zodac.folding.ejb.tc.lars;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
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
    private static final int MINIMUM_TD_ELEMENTS = 3;

    private LarsGpuParser() {

    }

    /**
     * Parses GPU {@link Elements} retrieved from LARS and parses each {@link Element} into a {@link LarsGpu}.
     *
     * <p>
     * If an error occurs parsing any individual {@link Element}, an error will be logged and the remainder will be parses.
     *
     * @param gpuEntries the LARS HTML data to parse
     * @return a {@link List} of the parsed {@link LarsGpu}s
     */
    static List<LarsGpu> parse(final Elements gpuEntries) {
        return gpuEntries
            .stream()
            .map(LarsGpuParser::parseSingleGpuEntry)
            .filter(Objects::nonNull)
            .collect(toList());
    }

    private static LarsGpu parseSingleGpuEntry(final Element gpuEntry) {
        try {
            return parseLarsGpu(gpuEntry);
        } catch (final ParseException e) {
            LOGGER.debug("Error parsing GPU entry: {}", gpuEntry, e);
            LOGGER.warn("Error parsing GPU entry: {}", e.getMessage());
            return null;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing GPU entry: {}", gpuEntry, e);
            return null;
        }
    }

    private static LarsGpu parseLarsGpu(final Element gpuEntry) throws ParseException {
        LOGGER.debug("Parsing GPU entry '{}'", gpuEntry);

        final String displayName = parseDisplayName(gpuEntry);
        final String manufacturer = parseManufacturer(gpuEntry, displayName);
        final String modelInfo = parseModelInfo(gpuEntry, displayName);
        final int rank = parseRank(gpuEntry, displayName);
        final long averagePpd = parseAveragePpd(gpuEntry, displayName);

        return LarsGpu.create(displayName, manufacturer, modelInfo, rank, averagePpd);
    }

    private static String parseDisplayName(final Element gpuEntry) throws ParseException {
        final Elements modelNames = gpuEntry.getElementsByClass("model-name");
        if (modelNames.isEmpty()) {
            throw new ParseException(String.format("Expected at least 1 'model-name' elements, found none for GPU entry: %s", gpuEntry));
        }

        final String modelName = modelNames.get(0).ownText();
        if (modelName.isEmpty()) {
            throw new ParseException(String.format("Empty 'model-name' for GPU entry: %s", gpuEntry));
        }

        return modelName;
    }

    private static String parseManufacturer(final Element gpuEntry, final String displayName) throws ParseException {
        final Elements spans = gpuEntry.getElementsByTag("span");

        if (spans.size() != EXPECTED_SPAN_ELEMENTS) {
            throw new ParseException(String.format("Expected %d 'span' element for manufacturer, found %d for GPU '%s'", EXPECTED_SPAN_ELEMENTS,
                spans.size(), displayName));
        }

        final String manufacturer = spans.get(2).ownText();
        if (manufacturer.isEmpty()) {
            throw new ParseException(String.format("No manufacturer for GPU '%s'", displayName));
        }

        return manufacturer;
    }

    private static String parseModelInfo(final Element gpuEntry, final String displayName) throws ParseException {
        final Elements modelInfos = gpuEntry.getElementsByClass("model-info");
        if (modelInfos.isEmpty()) {
            throw new ParseException(String.format("Expected at least 1 'model-info' elements, found none for GPU '%s'", displayName));
        }

        final String modelInfo = modelInfos.get(0).ownText();
        if (modelInfo.isEmpty()) {
            throw new ParseException(String.format("Empty 'model-info' for GPU '%s'", displayName));
        }

        return modelInfo;
    }

    private static int parseRank(final Element gpuEntry, final String displayName) throws ParseException {
        final Element rankElement = gpuEntry.getElementsByClass("rank-num").first();
        if (rankElement == null) {
            throw new ParseException(String.format("No 'rank-num' class element found for GPU '%s'", displayName));
        }

        if (rankElement.ownText().isBlank()) {
            throw new ParseException(String.format("No rank information for GPU '%s'", displayName));
        }

        try {
            return Integer.parseInt(rankElement.ownText());
        } catch (final NumberFormatException e) {
            throw new ParseException(String.format("Unable to convert rank into an integer for GPU '%s'", displayName), e);
        }
    }

    private static long parseAveragePpd(final Element gpuEntry, final String displayName) throws ParseException {
        final Elements tableEntries = gpuEntry.getElementsByTag("td");
        if (tableEntries.size() < MINIMUM_TD_ELEMENTS) {
            throw new ParseException(String.format("Expected at least %d 'td' elements, found %d for GPU '%s'", MINIMUM_TD_ELEMENTS,
                tableEntries.size(), displayName));
        }

        final String averagePpdWithCommas = tableEntries.get(2).ownText();
        if (averagePpdWithCommas.isBlank()) {
            throw new ParseException(String.format("No average PPD information for GPU '%s'", displayName));
        }

        final String averagePpdWithoutCommas = averagePpdWithCommas.replace(",", "");

        try {
            return Long.parseLong(averagePpdWithoutCommas);
        } catch (final NumberFormatException e) {
            throw new ParseException(String.format("Unable to convert rank into an long for GPU '%s'", displayName), e);
        }
    }
}
