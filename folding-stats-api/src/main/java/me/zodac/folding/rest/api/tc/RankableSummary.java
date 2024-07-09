/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Interface used to define a summary object can be ranked.
 */
public sealed interface RankableSummary permits RetiredUserSummary, TeamSummary, UserSummary {

    /**
     * Updates a {@link RankableSummary} with a rank, after it has been calculated.
     *
     * @param newRank the new rank for the {@link RankableSummary}
     * @return the updated {@link RankableSummary}
     */
    RankableSummary updateWithNewRank(int newRank);

    /**
     * The value of the {@link RankableSummary}, used to determine the rank.
     *
     * @return the value
     */
    long getRankableValue();

    /**
     * Ranks the input {@link Collection} of {@link RankableSummary}, then updated the {@code rank} field for each instance with the new rank. Applies
     * no offset to the ranks (see {@link #rank(Collection, int)}).
     *
     * @param objectsToRank the {@link RankableSummary} instances to rank
     * @return the {@link Collection} of {@link RankableSummary} with the rank values updated
     */
    static Collection<RankableSummary> rank(final Collection<? extends RankableSummary> objectsToRank) {
        return rank(objectsToRank, 0);
    }

    /**
     * Ranks the input {@link Collection} of {@link RankableSummary}, then updated the {@code rank} field for each instance with the new rank.
     *
     * @param objectsToRank the {@link RankableSummary} instances to rank
     * @param rankOffset    an offset to be applied to the final rank
     * @return the {@link Collection} of {@link RankableSummary} with the rank values updated
     */
    static Collection<RankableSummary> rank(final Collection<? extends RankableSummary> objectsToRank, final int rankOffset) {
        final List<? extends RankableSummary> sortedObjectsToRank = objectsToRank
            .stream()
            .sorted(Comparator.comparingLong(RankableSummary::getRankableValue).reversed())
            .toList();

        final int numberOfObjects = sortedObjectsToRank.size();
        final Collection<RankableSummary> rankedObjects = new ArrayList<>(numberOfObjects);
        long previousValue = 0;
        int previousRank = 1;

        for (int i = 0; i < numberOfObjects; i++) {
            final RankableSummary rankableSummary = sortedObjectsToRank.get(i);
            final long value = rankableSummary.getRankableValue();

            final int newRank;

            if (previousValue == value) {
                newRank = previousRank;
            } else {
                // We explicitly want to move to the next rank after a tie (1st, 1st, 3rd) and skipping the tied value
                // So we increment based on index rather than previousRank (which would give us 1st, 1st, 2nd)
                newRank = i + 1 + rankOffset;
            }

            rankedObjects.add(rankableSummary.updateWithNewRank(newRank));

            previousValue = value;
            previousRank = newRank;
        }

        return rankedObjects;
    }
}
