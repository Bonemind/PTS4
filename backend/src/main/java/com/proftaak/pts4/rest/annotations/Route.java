package com.proftaak.pts4.rest.annotations;

import com.proftaak.pts4.rest.HTTPMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register the method to handle request to a certain route
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {

    /**
     * The method this route should respond to
     */
    HTTPMethod method();

    /**
     * The route url
     */
    String path() default "";
}
