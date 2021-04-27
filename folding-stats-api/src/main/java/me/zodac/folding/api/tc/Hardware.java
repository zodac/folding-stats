package me.zodac.folding.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Hardware implements Identifiable {
    
    public static final int EMPTY_HARDWARE_ID = 0;

    private int id;
    private String hardwareName;
    private String displayName;
    private String operatingSystem;
    private double multiplier;

    public static Hardware create(final int id, final String hardwareName, final String displayName, final String os, final double multiplier) {
        return new Hardware(id, hardwareName, displayName, os, multiplier);
    }

    public static Hardware updateWithId(final int id, final Hardware hardware) {
        return new Hardware(id, hardware.hardwareName, hardware.displayName, hardware.operatingSystem, hardware.multiplier);
    }
}
