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

package me.zodac.folding.test.proto.steps;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.test.framework.TestStep;
import me.zodac.folding.test.integration.util.SystemCleaner;

/**
 * {@link TestStep}s used to execute test logic on the system.
 */
public final class SystemSteps {

    /**
     * Cleans the system of any existing {@link Hardware}, {@link me.zodac.folding.api.tc.Team}s and {@link me.zodac.folding.api.tc.User}s.
     */
    public static TestStep cleanSystem() {
        return new TestStep(
            "Cleans the system of all hardware, teams and users (and resets the IDs in the database to 1)",
            (testContext) -> SystemCleaner.cleanSystemForSimpleTests()
        );
    }

    private SystemSteps() {

    }
}
