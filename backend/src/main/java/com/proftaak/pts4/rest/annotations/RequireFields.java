package com.proftaak.pts4.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this when you want to only allow access to a route when the user is logged in
 *
 * @author Michon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireFields {
    /**
     * The required fields.
     */
    String[] fields() default {};
}