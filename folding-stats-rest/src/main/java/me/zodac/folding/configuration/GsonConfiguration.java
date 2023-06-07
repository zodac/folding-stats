/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import me.zodac.folding.rest.api.util.LocalDateTimeGsonTypeAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} class used to inject an instance of {@link Gson} with custom {@link com.google.gson.JsonDeserializer}s and
 * {@link com.google.gson.JsonSerializer}s.
 */
@Configuration
@ConditionalOnClass(Gson.class)
public class GsonConfiguration {

    /**
     * Returns an instance of {@link Gson} with the {@link LocalDateTimeGsonTypeAdapter} registered.
     *
     * @return the {@link Gson} instance
     */
    @Bean
    public Gson gson() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, LocalDateTimeGsonTypeAdapter.getInstance())
            .create();
    }
}
