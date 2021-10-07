package me.zodac.folding.test.stub;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

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
            StubbedLarsEndpoint.class,
            StubbedPointsEndpoint.class,
            StubbedUnitsEndpoint.class
        );
    }
}