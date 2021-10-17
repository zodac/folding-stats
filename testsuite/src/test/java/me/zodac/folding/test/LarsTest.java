package me.zodac.folding.test;

import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.LarsUtils.addGpusToLarsDb;
import static me.zodac.folding.test.util.rest.request.LarsUtils.deleteAllGpusFromLarsDb;
import static me.zodac.folding.test.util.rest.request.LarsUtils.manualLarsUpdate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the updated of all {@link me.zodac.folding.api.tc.Hardware} from the LARS DB.
 */
class LarsTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
        deleteAllGpusFromLarsDb();
    }

    @AfterEach
    void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
        deleteAllGpusFromLarsDb();
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasNoHardware_thenSystemIsNotUpdated() throws FoldingRestException {
        manualLarsUpdate();

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

        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L)
        );

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterUpdate)
            .as("Expected one hardware to have been added to the system")
            .hasSize(1);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasNoNewHardware_thenSystemIsNotUpdated() throws FoldingRestException {
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L)
        );

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected one hardware to have been added to the system after first update")
            .hasSize(1);

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no additional hardware to have been added to the system after second update")
            .hasSize(1);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasChangesToExistingHardware_thenExistingHardwareIsUpdated() throws FoldingRestException {
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L)
        );

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected one hardware to have been added to the system after first update")
            .hasSize(1);

        final Hardware hardwareAfterFirstUpdate = allHardwareAfterFirstUpdate.iterator().next();
        assertThat(hardwareAfterFirstUpdate)
            .as("Expected first hardware to match contents of provided LARS GPU")
            .extracting("hardwareName", "hardwareMake", "averagePpd")
            .containsExactly("Hardware #1", HardwareMake.NVIDIA, 1_000L);

        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "AMD", "Hardware #1", 1, 1_500L)
        );
        manualLarsUpdate();

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
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L),
            LarsGpu.create("Hardware2", "nVidia", "Hardware #2", 2, 900L)
        );

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected two hardwares to have been added to the system after first update")
            .hasSize(2);

        deleteAllGpusFromLarsDb();
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L)
        );
        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected one hardware to have been deleted from the system after second update")
            .hasSize(2);

        final Hardware hardwareAfterSecondUpdate = allHardwareAfterSecondUpdate.iterator().next();
        assertThat(hardwareAfterSecondUpdate.getHardwareName())
            .as("Expected second hardware to have been deleted from the system")
            .isNotEqualTo("Hardware #2");
    }

    @Test
    void whenUpdatingHardwareFromLars_givenLarsHasAddedSomeNewHardware_thenNewHardwareIsCreated() throws FoldingRestException {
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L),
            LarsGpu.create("Hardware2", "nVidia", "Hardware #2", 2, 900L)
        );

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected two hardwares to have been added to the system after first update")
            .hasSize(2);

        addGpusToLarsDb(
            LarsGpu.create("Hardware3", "AMD", "Hardware #3", 3, 750L)
        );
        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected one hardware to have been added to the system after second update")
            .hasSize(3);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenNewLarsEntryHasNoPpd_thenNewHardwareIsIgnored() throws FoldingRestException {
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 1_000L),
            LarsGpu.create("Hardware2", "nVidia", "Hardware #2", 2, 900L)
        );

        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterFirstUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected two hardwares to have been added to the system after first update")
            .hasSize(2);

        addGpusToLarsDb(
            LarsGpu.create("Hardware3", "nVidia", "Hardware #3", 3, 0L)
        );
        manualLarsUpdate();

        final Collection<Hardware> allHardwareAfterSecondUpdate = HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll());
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no hardware to have been added to the system after second update")
            .hasSize(2);
    }

    @Test
    void whenUpdatingHardwareFromLars_givenBestHardwareHasNewPpdValue_thenAllOtherHardwareMultipliersAreUpdated() throws FoldingRestException {
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 10_000L),
            LarsGpu.create("Hardware2", "nVidia", "Hardware #2", 2, 5_000L),
            LarsGpu.create("Hardware3", "nVidia", "Hardware #3", 3, 1_000L)
        );

        manualLarsUpdate();

        final List<Hardware> allHardwareAfterFirstUpdate = new ArrayList<>(HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll()));
        assertThat(allHardwareAfterFirstUpdate)
            .as("Expected three hardwares to have been added to the system after first update")
            .hasSize(3);

        assertThat(allHardwareAfterFirstUpdate.get(0))
            .as("Expected first hardware to have correct multiplier after first update: " + allHardwareAfterFirstUpdate)
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #1", 1.0D);
        assertThat(allHardwareAfterFirstUpdate.get(1))
            .as("Expected second hardware to have correct multiplier after first update")
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #2", 2.0D);
        assertThat(allHardwareAfterFirstUpdate.get(2))
            .as("Expected third hardware to have correct multiplier after first update")
            .extracting("hardwareName", "multiplier")
            .containsExactly("Hardware #3", 10.0D);

        deleteAllGpusFromLarsDb();
        addGpusToLarsDb(
            LarsGpu.create("Hardware1", "nVidia", "Hardware #1", 1, 20_000L),
            LarsGpu.create("Hardware2", "nVidia", "Hardware #2", 2, 5_000L),
            LarsGpu.create("Hardware3", "nVidia", "Hardware #3", 3, 1_000L)
        );
        manualLarsUpdate();

        final List<Hardware> allHardwareAfterSecondUpdate = new ArrayList<>(HardwareResponseParser.getAll(HARDWARE_REQUEST_SENDER.getAll()));
        assertThat(allHardwareAfterSecondUpdate)
            .as("Expected no hardware to have been added to the system after second update")
            .hasSize(3);

        assertThat(allHardwareAfterSecondUpdate.get(0))
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
}