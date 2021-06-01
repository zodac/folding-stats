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
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.User;

/**
 * POJO defining a single {@link TeamRequest} participating in the <code>Team Competition</code>. There is a limit on the number of users each team can have, defined by the
 * {@link Category} description.
 * <p>
 * While each {@link TeamRequest} is made up of {@link User}s we do not keep any reference to the {@link User} in this object.
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
