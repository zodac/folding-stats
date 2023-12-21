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

import io.micrometer.core.instrument.MeterRegistry;
import me.zodac.folding.FoldingStatsApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} class used to define variables for a Prometheus {@link MeterRegistry}.
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Defines the tags for the Prometheus {@link MeterRegistry}.
     *
     * @param applicationName the name of the {@link FoldingStatsApplication}
     * @return the {@link MeterRegistryCustomizer} for Prometheus
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsTags(@Value("${application.name}") final String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }
}
