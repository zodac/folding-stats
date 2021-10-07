package me.zodac.folding.ejb.tc.lars;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
final class LarsGpuRetriever {

    private static final Logger LOGGER = LogManager.getLogger();

    private LarsGpuRetriever() {

    }

    /**
     * Will retrieve the {@link me.zodac.folding.api.tc.HardwareType#GPU} data from the supplied DB URL.
     *
     * <p>
     * If an error occurs retrieving any {@link LarsGpu}s, it will be
     *
     * @param gpuDatabaseUrl the URL to the LARS GPU database
     * @return a {@link Collection} of parsed {@link LarsGpu}s sorted by {@link LarsGpu#getRank()}
     */
    static List<LarsGpu> retrieveGpus(final String gpuDatabaseUrl) {
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

            return LarsGpuParser.parse(databaseEntries);
        } catch (final IOException e) {
            LOGGER.warn("Unable to connect to LARS GPU DB at '{}'", gpuDatabaseUrl, e);
            return Collections.emptyList();
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error retrieving data from LARS GPU DB", e);
            return Collections.emptyList();
        }
    }
}
