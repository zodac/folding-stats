/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
 *
 */

package me.zodac.folding.rest.api.tc.lars;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.lars.LarsGpu;
import org.springframework.stereotype.Service;

/**
 * Retrieves the latest {@link me.zodac.folding.api.tc.Hardware} information from LARS for {@link me.zodac.folding.api.tc.HardwareType}s, then updates
 * the database with the new values.
 */
@Service
public interface LarsHardwareUpdaterService {

    /**
     * Retrieves {@link me.zodac.folding.api.tc.HardwareType} data from LARS, creates {@link Hardware} instances, then updates to the DB.
     *
     * <p>
     * Depending on the state of the retrieved {@link Hardware}, one of the following will occur:
     * <ul>
     *     <li>
     *         If the {@link Hardware} is retrieved from LARS and does not exist in the DB, it will be <b>created</b>.
     *     </li>
     *     <li>
     *         If the {@link Hardware} is exists in the DB but is not retrieved from LARS, it will be <b>deleted</b>.
     *     </li>
     *     <li>
     *         If the {@link Hardware} is retrieved from LARS and does exist in the DB and either the {@code multiplier} or {@code averagePpd} is
     *         different, it will be <b>updated</b>.
     *     </li>
     *     <li>
     *         If the {@link Hardware} is retrieved from LARS and does exist in the DB and neither the {@code multiplier} nor {@code averagePpd} is
     *         different, it will be <b>ignored</b>.
     *     </li>
     * </ul>
     *
     * <p>
     * Since each {@link Hardware} requires a {@code multiplier}, we will manually calculate it based off the highest-ranked
     * {@link LarsGpu} that is retrieved. The formula for the {@code multiplier} is:
     * <pre>
     *     (PPD of best {@link LarsGpu}) / (PPD of current {@link LarsGpu})
     * </pre>
     */
    void retrieveHardwareAndPersist();
}