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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import me.zodac.folding.api.exception.LarsParseException;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LarsGpuParser}.
 */
class LarsGpuParserTest {

    @Test
    void validGpu() throws URISyntaxException, IOException, LarsParseException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        final LarsGpu result = LarsGpuParser.parseSingleGpuEntry(inputHtml);

        assertThat(result)
            .isEqualTo(inputLarsGpu);
    }

    @Test
    void noDisplayNameElement() throws URISyntaxException, IOException {
        final Document inputHtml = readFromFile("noModelNameSpanElement.txt");

        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("Expected at least 1 'model-name' elements");
    }

    @Test
    void blankDisplayName() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", "",
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("Empty 'model-name' for GPU entry");
    }

    @Test
    void noManufacturerElement() throws URISyntaxException, IOException {
        final Document inputHtml = readFromFile("noManufacturerSpanElement.txt");

        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContainingAll(
                "Expected ",
                "'span' elements for manufacturer"
            );
    }

    @Test
    void blankManufacturer() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", "",
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("No manufacturer for GPU '" + inputLarsGpu.getDisplayName() + "'");
    }

    @Test
    void noModelInfoElement() throws URISyntaxException, IOException {
        final Document inputHtml = readFromFile("noModelInfoSpanElement.txt");

        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("Expected at least 1 'model-info' elements");
    }

    @Test
    void blankModelInfo() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", "",
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("Empty 'model-info' for GPU '" + inputLarsGpu.getDisplayName() + "'");
    }

    @Test
    void noRankElement() throws URISyntaxException, IOException {
        final Document inputHtml = readFromFile("noRankElement.txt");

        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("No 'rank-num' class element found for GPU");
    }

    @Test
    void zeroRank() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", "0",
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining(String.format("Unable to use GPU '%s' as it has a rank of '0'", inputLarsGpu.getDisplayName()));
    }

    @Test
    void negativeRank() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", "-1",
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining(String.format("Unable to use GPU '%s' as it has a rank of '-1'", inputLarsGpu.getDisplayName()));
    }

    @Test
    void invalidRank() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", "invalidRank",
            "averagePpd", String.valueOf(inputLarsGpu.getAveragePpd())
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("Unable to convert rank into an integer for GPU '" + inputLarsGpu.getDisplayName() + "'");
    }

    @Test
    void noAveragePpdElement() throws URISyntaxException, IOException {
        final Document inputHtml = readFromFile("noAveragePpdElement.txt");

        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContainingAll(
                "Expected at least ",
                "'td' elements"
            );
    }

    @Test
    void zeroAveragePpd() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", "0"
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining(String.format("Unable to use GPU '%s' as it has an averagePpd of '0'", inputLarsGpu.getDisplayName()));
    }

    @Test
    void negativeAveragePpd() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", "-1"
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining(String.format("Unable to use GPU '%s' as it has an averagePpd of '-1'", inputLarsGpu.getDisplayName()));
    }

    @Test
    void invalidAveragePpd() throws URISyntaxException, IOException {
        final LarsGpu inputLarsGpu = LarsGpu.create("displayName", "manufacturer", "modelInfo", 1, 1_000_000L);
        final Map<String, String> substitutionValues = Map.of(
            "displayName", inputLarsGpu.getDisplayName(),
            "manufacturer", inputLarsGpu.getManufacturer(),
            "modelInfo", inputLarsGpu.getModelInfo(),
            "rank", String.valueOf(inputLarsGpu.getRank()),
            "averagePpd", "invalidAveragePpd"
        );

        final Document inputHtml = readFromFile("validGpuTemplate.txt", substitutionValues);
        assertThatThrownBy(() -> LarsGpuParser.parseSingleGpuEntry(inputHtml))
            .isInstanceOf(LarsParseException.class)
            .hasMessageContaining("Unable to convert averagePpd into a long for GPU '" + inputLarsGpu.getDisplayName() + "'");
    }

    private static Document readFromFile(final String fileName) throws URISyntaxException, IOException {
        return readFromFile(fileName, Collections.emptyMap());
    }

    private static Document readFromFile(final String fileName, final Map<String, String> substitutionValues) throws URISyntaxException, IOException {
        final String filePath = "lars" + File.separator + fileName;
        final String fileContents =
            Files.readString(Paths.get(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(filePath)).toURI()),
                StandardCharsets.UTF_8);

        if (substitutionValues.isEmpty()) {
            return Jsoup.parse(fileContents);
        }

        final String fileContentsWithSubstitutions = substituteValues(fileContents, substitutionValues);
        return Jsoup.parse(fileContentsWithSubstitutions);
    }

    private static String substituteValues(final String template, final Map<String, String> substitutionValues) {
        final StringSubstitutor substitutor = new StringSubstitutor(substitutionValues);
        return substitutor.replace(template);
    }
}
