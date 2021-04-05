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
        final Set<Class<?>> classes = new HashSet<>(4); // TODO: [zodac] Set to correct initial capacity when done
        classes.add(HardwareEndpoint.class);
        classes.add(UserEndpoint.class);
        classes.add(TeamEndpoint.class);
        classes.add(TeamCompetitionStatsEndpoint.class);

        // TODO: [zodac] Add a Healthcheck endpoint for liveliness/readiness probes (though we probably won't ever be in K8S)?
        return classes;
    }

}