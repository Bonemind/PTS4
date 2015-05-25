package com.proftaak.pts4.rest.annotations;

import com.proftaak.pts4.rest.ScopeRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limit access to all route to logged in users
 * <p>
 * Optionally, also limit it to user with certain roles within the current scope
 * <p>
 * Can only be used in conjunction with @Route
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireAuth {
    /**
     * The allowed role
     */
    ScopeRole role() default ScopeRole.USER;
}