package me.zodac.folding.lars;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.zodac.folding.api.exception.LarsParseException;
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

    private static final Logger LOGGER = LogManager.getLogger();

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
     */
    public static List<LarsGpu> retrieveGpus(final String gpuDatabaseUrl) {
        LOGGER.debug("Retrieving LARS GPU data from: '{}'", gpuDatabaseUrl);

        try {
            final Document doc = Jsoup.connect(gpuDatabaseUrl).get();
            final Element databaseTable = doc.getElementById("primary-datatable"); // Should be non-null

            if (databaseTable == null) {
                LOGGER.warn("Unable to find the 'primary-database' table");
                return Collections.emptyList();
            }

            final Element databaseBody = databaseTable.getElementsByTag("tbody").first();

            if (databaseBody == null) {
                LOGGER.warn("Unable to find a 'tbody' entry in the database table");
                return Collections.emptyList();
            }

            final Elements databaseEntries = databaseBody.getElementsByTag("tr");
            if (databaseEntries.isEmpty()) {
                LOGGER.warn("No 'tr' entries found in the database table");
                return Collections.emptyList();
            }

            return databaseEntries
                .stream()
                .map(LarsGpuRetriever::safeParse)
                .filter(Objects::nonNull)
                .sorted(LarsGpuAveragePpdComparator.create())
                .collect(toList());
        } catch (final IOException e) {
            LOGGER.debug("Unable to connect to LARS GPU DB at '{}'", gpuDatabaseUrl, e);
            LOGGER.warn("Unable to connect to LARS GPU DB at '{}': {}", gpuDatabaseUrl, e.getMessage());
            return Collections.emptyList();
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error retrieving data from LARS GPU DB", e);
            return Collections.emptyList();
        }
    }

    private static LarsGpu safeParse(final Element element) {
        try {
            return LarsGpuParser.parseSingleGpuEntry(element);
        } catch (final LarsParseException e) {
            LOGGER.debug("Error parsing GPU entry: {}", element, e);
            LOGGER.warn("Error parsing GPU entry: {}", e.getMessage());
            return null;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing GPU entry: {}", element, e);
            return null;
        }
    }
}
