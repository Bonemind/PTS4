package com.proftaak.pts4.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a controller
 * <p>
 * Controllers are collections of routes with a common theme/goal
 * This usually means they have the same starting path as well, although that is not required
 * <p>
 * This is required to be able to use @Route and @PreRequest
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
}
