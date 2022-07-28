/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    static Collection<? extends RankableSummary> rank(final Collection<? extends RankableSummary> objectsToRank) {
        return rank(objectsToRank, 0);
    }

    /**
     * Ranks the input {@link Collection} of {@link RankableSummary}, then updated the {@code rank} field for each instance with the new rank.
     *
     * @param objectsToRank the {@link RankableSummary} instances to rank
     * @param rankOffset    an offset to be applied to the final rank
     * @return the {@link Collection} of {@link RankableSummary} with the rank values updated
     */
    static Collection<? extends RankableSummary> rank(final Collection<? extends RankableSummary> objectsToRank, final int rankOffset) {
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
