package me.zodac.folding.api.state;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation defining a function that will perform {@link OperationType#WRITE} operations on the system.
 *
 * <p>
 * To be used to check against a {@link SystemState}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WriteRequired {

}
