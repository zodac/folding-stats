package me.zodac.folding.rest.api.tc.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.RequestPojo;


@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class HardwareRequest implements RequestPojo {


    public static final int EMPTY_HARDWARE_ID = 0;

    private int id;
    private String hardwareName;
    private String displayName;
    private String operatingSystem;
    private double multiplier;
}
