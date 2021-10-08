package me.zodac.folding.lars;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import me.zodac.folding.api.tc.Hardware;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareSplitter}.
 */
class HardwareSplitterTest {

    @Test
    void testToCreate() {
        final Collection<Hardware> lars = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .build()
        );
        final Collection<Hardware> existing = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .build()
        );

        final Collection<Hardware> toCreate = HardwareSplitter.toCreate(lars, existing);
        final Collection<String> names = toCreate.stream().map(Hardware::getHardwareName).collect(Collectors.toList());

        assertThat(names)
            .hasSize(2)
            .contains(
                "Test2",
                "Test3"
            );
    }

    @Test
    void testToDelete() {
        final Collection<Hardware> lars = Set.of(
            Hardware.builder()
                .hardwareName("Test2")
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .build()
        );
        final Collection<Hardware> existing = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .build()
        );

        final Collection<Hardware> toDelete = HardwareSplitter.toDelete(lars, existing);
        final Collection<String> names = toDelete.stream().map(Hardware::getHardwareName).collect(Collectors.toList());

        assertThat(names)
            .hasSize(1)
            .contains(
                "Test1"
            );
    }

    @Test
    void testToUpdate() {
        final Collection<Hardware> lars = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .multiplier(1.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .multiplier(1.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .multiplier(1.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test4")
                .multiplier(1.00D)
                .build()
        );
        final Collection<Hardware> existing = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .multiplier(2.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .multiplier(2.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .multiplier(1.00D)
                .build()
        );

        final Map<Hardware, Hardware> toUpdate = HardwareSplitter.toUpdate(lars, existing);
        final Collection<String> names = toUpdate.keySet().stream().map(Hardware::getHardwareName).collect(Collectors.toList());

        assertThat(names)
            .hasSize(2)
            .contains(
                "Test1",
                "Test2"
            );
    }
}
