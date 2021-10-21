package me.zodac.folding.ejb.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marker annotation to define a function in {@link Storage} that persists and retrieves data from a database, but does <b>not</b> cache that data locally.
 */
@Documented
@Target(ElementType.METHOD)
@interface NotCached {

}
