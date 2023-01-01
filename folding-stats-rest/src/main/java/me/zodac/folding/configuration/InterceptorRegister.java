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

import me.zodac.folding.rest.interceptor.SecurityInterceptor;
import me.zodac.folding.rest.interceptor.StateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@link Configuration} defining the implementations of {@link HandlerInterceptor} to be registered to the
 * {@link org.springframework.boot.autoconfigure.SpringBootApplication}.
 */
@Configuration
public class InterceptorRegister implements WebMvcConfigurer {

    private final SecurityInterceptor securityInterceptor;
    private final StateInterceptor stateInterceptor;

    /**
     * {@link Autowired} constructor.
     *
     * @param securityInterceptor the {@link SecurityInterceptor}
     * @param stateInterceptor    the {@link StateInterceptor}
     */
    @Autowired
    public InterceptorRegister(final SecurityInterceptor securityInterceptor, final StateInterceptor stateInterceptor) {
        this.securityInterceptor = securityInterceptor;
        this.stateInterceptor = stateInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry
            .addInterceptor(securityInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/error", "/login/admin");

        registry
            .addInterceptor(stateInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/error", "/login/admin");
    }
}
