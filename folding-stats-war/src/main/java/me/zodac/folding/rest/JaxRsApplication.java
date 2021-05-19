package me.zodac.folding.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
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
        final Set<Class<?>> services = new HashSet<>(6);
        // REST endpoints
        services.add(HardwareEndpoint.class);
        services.add(HistoricStatsEndpoint.class);
        services.add(UserEndpoint.class);
        services.add(TeamEndpoint.class);
        services.add(TeamCompetitionStatsEndpoint.class);

        // REST providers
        services.add(InvalidUrlHandler.class);
        return services;
    }
}