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
 * POJO defining a single {@link TeamRequest} participating in the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamRequest implements RequestPojo {

    /**
     * The default {@link TeamRequest} ID. Since the REST request would not know the ID until the DB has created the object,
     * we use this and update the ID later.
     */
    public static final int EMPTY_TEAM_ID = 0;

    private int id;
    private String teamName;
    private String teamDescription;
    private String forumLink;
}
