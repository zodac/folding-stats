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

/**
 * REST request to create/update a {@link me.zodac.folding.api.tc.User}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserRequest implements RequestPojo {

    private String foldingUserName;
    private String displayName;
    private String passkey;
    private String category;
    private String profileLink;
    private String liveStatsLink;
    private int hardwareId;
    private int teamId;
    private boolean userIsCaptain;
}
