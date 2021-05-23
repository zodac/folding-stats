package me.zodac.folding.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * A class extending {@link Application} and annotated with {@link ApplicationPath} is the Java EE 6 "no XML" approach to activating JAX-RS.
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath} annotation.
 */
@ApplicationPath("/")
public class JaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                // REST endpoints
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