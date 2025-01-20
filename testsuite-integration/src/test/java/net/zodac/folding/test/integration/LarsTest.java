/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package net.zodac.folding.test.integration;

import static net.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.lars.LarsGpu;
import net.zodac.folding.client.java.response.HardwareResponseParser;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.test.integration.util.SystemCleaner;
import net.zodac.folding.test.integration.util.rest.request.LarsUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the updated of all {@link Hardware} from the LARS DB.
 */
class LarsTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        SystemCleaner.cleanSystemForSimpleTests();
        LarsUtils.deleteAllGpusFromLarsDb();
    }

    @AfterEach
    void tearDown() throws FoldingRestException {
        setUp();
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasNoHardware_thenSystemIsNotUpdated() throws FoldingRestException {
        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterUpdate)
            .as("Expected no hardware to have been added to the system")
            .isEmpty();
    }

    @Test
    void whenUpdatingHardwareFromLars_givenSystemHasNoHardware_thenSystemIsUpdatedWithLarsHardware() throws FoldingRestException {
        final Collection<Hardware> allHardwareBeforeUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareBeforeUpdate)
            .as("Expected there to be no hardware in the system when starting the test")
            .isEmpty();

        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D)
        );

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterUpdate)
            .as("Expected one hardware to have been added to the system")
            .hasSize(1);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasNoNewHardware_thenSystemIsNotUpdated() throws FoldingRestException {
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D)
        );

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected one hardware to have been added to the system after first update")
            .hasSize(1);

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no additional hardware to have been added to the system after second update")
            .hasSize(1);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasChangesToExistingHardware_thenExistingHardwareIsUpdated() throws FoldingRestException {
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D)
        );

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected one hardware to have been added to the system after first update")
            .hasSize(1);

        final Hardware hardwareAfterFirstUpdate = allHardwareAfterFirstUpdate.iterator().next();
        assertThat(hardwareAfterFirstUpdate)
            .as("Expected first hardware to match contents of provided LARS GPU")
            .extracting("hardwareName", "hardwareMake", "averagePpd")
            .containsExactly("Hardware #1", HardwareMake.NVIDIA, 1_000L);

        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.AMD, "Hardware #1", 1, 1_500L, 1.00D)
        );
        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no additional hardware to have been added to the system after second update")
            .hasSize(1);

        final Hardware hardwareAfterSecondUpdate = allHardwareAfterSecondUpdate.iterator().next();
        assertThat(hardwareAfterSecondUpdate)
            .as("Expected first hardware to be updated match contents of updated LARS GPU")
            .extracting("hardwareName", "hardwareMake", "averagePpd")
            .containsExactly("Hardware #1", HardwareMake.AMD, 1_500L);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasRemovedSomeExistingHardware_thenThatExistingHardwareIsDeleted() throws FoldingRestException {
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D),
            create("Hardware2", HardwareMake.NVIDIA, "Hardware #2", 2, 900L, 1.11D)
        );

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected two hardwares to have been added to the system after first update")
            .hasSize(2);

        LarsUtils.deleteAllGpusFromLarsDb();
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D)
        );
        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected one hardware to have been deleted from the system after second update")
            .hasSize(2);

        final Hardware hardwareAfterSecondUpdate = allHardwareAfterSecondUpdate.iterator().next();
        assertThat(hardwareAfterSecondUpdate.hardwareName())
            .as("Expected second hardware to have been deleted from the system")
            .isNotEqualTo("Hardware #2");
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasAddedSomeNewHardware_thenNewHardwareIsCreated() throws FoldingRestException {
        final Collection<Hardware> initialHardware = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        final int numberOfInitialHardware = initialHardware.size();

        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D),
            create("Hardware2", HardwareMake.NVIDIA, "Hardware #2", 2, 900L, 1.11D)
        );

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected two hardwares to have been added to the system after first update")
            .hasSize(numberOfInitialHardware + 2);

        LarsUtils.addGpusToLarsDb(
            create("Hardware3", HardwareMake.AMD, "Hardware #3", 3, 750L, 1.33D)
        );
        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected one more hardware to have been added to the system after second update")
            .hasSize(allHardwareAfterFirstUpdate.size() + 1);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenNewLarsEntryHasNoPpd_thenNewHardwareIsIgnored() throws FoldingRestException {
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 1_000L, 1.00D),
            create("Hardware2", HardwareMake.NVIDIA, "Hardware #2", 2, 900L, 1.11D)
        );

        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected two hardwares to have been added to the system after first update")
            .hasSize(2);

        LarsUtils.addGpusToLarsDb(
            create("Hardware3", HardwareMake.NVIDIA, "Hardware #3", 3, 0L, 0.00D)
        );
        LarsUtils.manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no hardware to have been added to the system after second update")
            .hasSize(2);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenBestHardwareHasNewPpdValue_thenAllOtherHardwareMultipliersAreUpdated() throws FoldingRestException {
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 10_000L, 1.00D),
            create("Hardware2", HardwareMake.NVIDIA, "Hardware #2", 2, 5_000L, 2.00D),
            create("Hardware3", HardwareMake.NVIDIA, "Hardware #3", 3, 1_000L, 10.00D)
        );

        LarsUtils.manualLarsUpdate();

        final List<Hardware> allHardwareAfterFirstUpdate = new ArrayList<>(HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll()));
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected three hardwares to have been added to the system after first update")
            .hasSize(3);

        assertThat(allHardwareAfterFirstUpdate.getFirst())
            .as("Expected first hardware to have correct multiplier after first update: %s", allHardwareAfterFirstUpdate)
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #1", 1.0D);
        assertThat(allHardwareAfterFirstUpdate.get(1))
            .as("Expected second hardware to have correct multiplier after first update: %s", allHardwareAfterFirstUpdate)
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #2", 2.0D);
        assertThat(allHardwareAfterFirstUpdate.get(2))
            .as("Expected third hardware to have correct multiplier after first update: %s", allHardwareAfterFirstUpdate)
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #3", 10.0D);

        LarsUtils.deleteAllGpusFromLarsDb();
        LarsUtils.addGpusToLarsDb(
            create("Hardware1", HardwareMake.NVIDIA, "Hardware #1", 1, 20_000L, 1.00D),
            create("Hardware2", HardwareMake.NVIDIA, "Hardware #2", 2, 5_000L, 4.00D),
            create("Hardware3", HardwareMake.NVIDIA, "Hardware #3", 3, 1_000L, 20.00D)
        );
        LarsUtils.manualLarsUpdate();

        final List<Hardware> allHardwareAfterSecondUpdate = new ArrayList<>(HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll()));
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no hardware to have been added to the system after second update")
            .hasSize(3);

        assertThat(allHardwareAfterSecondUpdate.getFirst())
            .as("Expected first hardware to have an unchanged multiplier after second update")
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #1", 1.0D);
        assertThat(allHardwareAfterSecondUpdate.get(1))
            .as("Expected second hardware to have an updated multiplier after second update")
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #2", 4.0D);
        assertThat(allHardwareAfterSecondUpdate.get(2))
            .as("Expected third hardware to have an updated multiplier after second update")
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #3", 20.0D);
    }

    private static LarsGpu create(final String displayName,
                                  final HardwareMake hardwareMake,
                                  final String modelInfo,
                                  final int rank,
                                  final long averagePpd,
                                  final double multiplier) {
        return new LarsGpu(
            displayName,
            modelInfo,
            hardwareMake.name(),
            rank,
            multiplier,
            averagePpd
        );
    }
}
