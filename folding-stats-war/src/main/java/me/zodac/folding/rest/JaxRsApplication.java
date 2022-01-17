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

package me.zodac.folding.rest;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import me.zodac.folding.rest.endpoint.DebugEndpoint;
import me.zodac.folding.rest.endpoint.HardwareEndpoint;
import me.zodac.folding.rest.endpoint.HistoricStatsEndpoint;
import me.zodac.folding.rest.endpoint.LoginEndpoint;
import me.zodac.folding.rest.endpoint.MonthlyResultEndpoint;
import me.zodac.folding.rest.endpoint.TeamCompetitionStatsEndpoint;
import me.zodac.folding.rest.endpoint.TeamEndpoint;
import me.zodac.folding.rest.endpoint.UserEndpoint;
import me.zodac.folding.rest.provider.CorsFilter;
import me.zodac.folding.rest.provider.InvalidUrlRedirecter;
import me.zodac.folding.rest.provider.interceptor.StateInterceptor;
import me.zodac.folding.rest.provider.interceptor.security.SecurityInterceptor;

/**
 * A class extending {@link Application} and annotated with {@link ApplicationPath} is the Java EE 6 "no XML" approach to activating JAX-RS.
 *
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath} annotation.
 */
@ApplicationPath("/")
public class JaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            // REST endpoints
            DebugEndpoint.class,
            HardwareEndpoint.class,
            HistoricStatsEndpoint.class,
            LoginEndpoint.class,
            MonthlyResultEndpoint.class,
            TeamCompetitionStatsEndpoint.class,
            TeamEndpoint.class,
            UserEndpoint.class,

            // REST providers
            CorsFilter.class,
            InvalidUrlRedirecter.class,
            SecurityInterceptor.class,
            StateInterceptor.class
        );
    }
}