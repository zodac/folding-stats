/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.rest.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Simple utility class holding some constants.
 */
public final class RestUtilConstants {

    /**
     * Instance of {@link Gson} with:
     * <ul>
     *     <li>Pretty-printing enabled</li>
     *     <li>HTML escaping disabled</li>
     *     <li>Custom {@link LocalDateTimeGsonTypeAdapter} for {@link LocalDateTime}</li>
     * </ul>
     */
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, LocalDateTimeGsonTypeAdapter.getInstance())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    /**
     * Instance of {@link HttpClient} with:
     * <ul>
     *     <li>HTTP protocol of {@link HttpClient.Version#HTTP_2}</li>
     *     <li>A connection timeout of <b>10</b> {@link java.util.concurrent.TimeUnit#SECONDS}</li>
     * </ul>
     */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(10L))
        .build();

    private RestUtilConstants() {

    }
}
