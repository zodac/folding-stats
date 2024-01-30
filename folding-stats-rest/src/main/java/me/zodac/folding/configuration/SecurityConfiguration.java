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

package me.zodac.folding.configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * {@link Configuration} class used configure Spring security related options.
 */
@Configuration
public class SecurityConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> ALLOWED_HEADERS = List.of("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method",
        "Authorization", "Content-Type", "Origin", "X-Requested-With");
    private static final List<String> ALLOWED_METHODS = List.of("DELETE", "FETCH", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT");
    private static final List<String> EXPOSED_HEADERS = List.of("Access-Control-Allow-Credentials", "Access-Control-Allow-Origin", "Cache-Control",
        "Content-Language", "Content-Length", "Content-Type");
    private static final Duration MAX_AGE = Duration.of(1, ChronoUnit.HOURS);

    /**
     * Disables CSRF and configures CORS for the application.
     *
     * @return the configured {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) {
        try {
            return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
        } catch (final Exception e) {
            LOGGER.error("Error configuring CSRF/CORS", e);
            throw new IllegalStateException("Unable to configure CSRF/CORS", e);
        }
    }

    /**
     * Configures CORS for the application for all origins.
     *
     * @return the {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(false);
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setMaxAge(MAX_AGE);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
