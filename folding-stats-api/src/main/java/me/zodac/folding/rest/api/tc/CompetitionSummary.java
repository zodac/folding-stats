/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Summary of the total stats of the {@code Team Competition}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class CompetitionSummary {

    private long totalPoints;
    private long totalMultipliedPoints;
    private int totalUnits;

    /**
     * Creates a {@link CompetitionSummary}.
     *
     * @param totalPoints           total unmultiplied points for all {@link TeamSummary}s
     * @param totalMultipliedPoints total multiplied points for all {@link TeamSummary}s
     * @param totalUnits            total units for all {@link TeamSummary}s
     * @return the created {@link AllTeamsSummary}
     */
    public static CompetitionSummary create(final long totalPoints, final long totalMultipliedPoints, final int totalUnits) {
        return new CompetitionSummary(totalPoints, totalMultipliedPoints, totalUnits);
    }
}
