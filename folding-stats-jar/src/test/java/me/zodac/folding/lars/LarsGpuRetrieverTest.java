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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LarsGpuRetriever}.
 */
class LarsGpuRetrieverTest {

    @Test
    void whenRetrieveGpus_givenDocumentHasValidData_thenParsedLarsGpusAreReturned() throws URISyntaxException, IOException {
        final Document document = readFromFile("validData.html");
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus(document);
        assertThat(result)
            .hasSize(5);
    }

    @Test
    void whenRetrieveGpus_givenDocumentHasNoPrimaryTable_thenEmptyListIsReturned() throws URISyntaxException, IOException {
        final Document document = readFromFile("noPrimaryTable.html");
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus(document);
        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenRetrieveGpus_givenDocumentHasNoTableBody_thenEmptyListIsReturned() throws URISyntaxException, IOException {
        final Document document = readFromFile("noTableBody.html");
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus(document);
        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenRetrieveGpus_givenDocumentHasNoTableEntries_thenEmptyListIsReturned() throws URISyntaxException, IOException {
        final Document document = readFromFile("noTableEntries.html");
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus(document);
        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenRetrieveGpus_givenDocumentHasMalformedData_thenEmptyListIsReturned() throws URISyntaxException, IOException {
        final Document document = readFromFile("malformedData.html");
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus(document);
        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenRetrieveGpus_givenDocumentHasGpuEntryFailingValidation_thenInvalidEntryIsExcludedListIsReturned()
        throws URISyntaxException, IOException {
        final Document document = readFromFile("singleValidationFailure.html");
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus(document);
        assertThat(result)
            .hasSize(4);
    }

    @Test
    void whenRetrieveGpus_givenInvalidUrl_thenEmptyCollectionIsReturned() {
        final Collection<LarsGpu> result = LarsGpuRetriever.retrieveGpus("invalidUrl");
        assertThat(result)
            .isEmpty();
    }

    private static Document readFromFile(final String fileName) throws URISyntaxException, IOException {
        final String filePath = "lars" + File.separator + "retriever" + File.separator + fileName;
        final String fileContents = Files.readString(
            Paths.get(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(filePath)).toURI()),
            StandardCharsets.UTF_8);

        return Jsoup.parse(fileContents);
    }
}
