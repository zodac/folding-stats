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

package net.zodac.folding.rest.api.tc.request;

import static net.zodac.folding.api.util.StringUtils.isBlank;
import static net.zodac.folding.api.util.StringUtils.isBlankOrValidUrl;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.stream.Stream;
import net.zodac.folding.api.RequestPojo;
import net.zodac.folding.api.exception.ValidationException;
import net.zodac.folding.api.tc.Team;
import org.jspecify.annotations.Nullable;

/**
 * REST request to create/update a {@link Team}.
 */
@Schema(name = "TeamRequest",
    description = "An example request to create a team, with all fields",
    example = """
        {
          "teamName": "Team1",
          "teamDescription": "The greatest team in the world!",
          "forumLink": "https://extremehw.net/forum/125-extreme-team-folding/"
        }"""
)
public record TeamRequest(
    @Schema(
        description = "The unique name of the team",
        example = "Team1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    String teamName,
    @Schema(
        description = "A description or motto for the team",
        example = "The greatest team in the world!",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Nullable
    String teamDescription,
    @Schema(
        description = "A link to the team on the forum",
        example = "https://extremehw.net/forum/125-extreme-team-folding/",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Nullable
    String forumLink
) implements RequestPojo {

    /**
     * Simple check that validates that the REST payload is valid. Checks that:
     * <ul>
     *     <li>'teamName' is not null or empty</li>
     *     <li>'forumLink' is null or empty, or else is a valid {@link java.net.URI}</li>
     * </ul>
     *
     * @throws ValidationException thrown if there are any validation failures
     */
    public void validate() {
        final Collection<String> failureMessages = Stream.of(
                validateTeamName(),
                validateForumLink()
            )
            .filter(s -> !s.isEmpty())
            .toList();
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(this, failureMessages);
        }
    }

    private String validateTeamName() {
        return isBlank(teamName)
            ? "Field 'teamName' must not be empty"
            : "";
    }

    private String validateForumLink() {
        return isBlankOrValidUrl(forumLink)
            ? ""
            : String.format("Field 'forumLink' is not a valid link: '%s'", forumLink);
    }
}
