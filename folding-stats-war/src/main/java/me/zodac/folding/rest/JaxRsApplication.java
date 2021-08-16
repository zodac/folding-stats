package me.zodac.folding.rest;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import me.zodac.folding.rest.endpoint.DebugEndpoint;
import me.zodac.folding.rest.endpoint.HardwareEndpoint;
import me.zodac.folding.rest.endpoint.HistoricStatsEndpoint;
import me.zodac.folding.rest.endpoint.LoginEndpoint;
import me.zodac.folding.rest.endpoint.TeamCompetitionStatsEndpoint;
import me.zodac.folding.rest.endpoint.TeamEndpoint;
import me.zodac.folding.rest.endpoint.UserEndpoint;
import me.zodac.folding.rest.provider.CorsFilter;
import me.zodac.folding.rest.provider.InvalidUrlRedirecter;
import me.zodac.folding.rest.provider.security.SecurityInterceptor;

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
            TeamCompetitionStatsEndpoint.class,
            TeamEndpoint.class,
            UserEndpoint.class,

            // REST providers
            CorsFilter.class,
            InvalidUrlRedirecter.class,
            SecurityInterceptor.class
        );
    }
}