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
