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

package net.zodac.folding.rest.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Month;
import java.time.Year;
import net.zodac.folding.api.tc.result.MonthlyResult;
import net.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * REST endpoints for {@code Team Competition} {@link MonthlyResult}s.
 */
public interface MonthlyResultEndpoint {

    /**
     * {@link GetMapping} request that retrieves a {@link MonthlyResult} for the given {@link Month}/{@link Year}.
     *
     * @param year    the {@link Year} of the {@link MonthlyResult}
     * @param month   the {@link Month} of the {@link MonthlyResult}
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object)} with the {@link MonthlyResult}
     */
    ResponseEntity<MonthlyResult> getMonthlyResult(@PathVariable("year") String year,
                                                   @PathVariable("month") String month,
                                                   HttpServletRequest request);

    /**
     * {@link PostMapping} request that performs a manual save of the current {@link MonthlyResult}.
     *
     * @return {@link Responses#ok()}
     */
    ResponseEntity<Void> saveMonthlyResult();
}
