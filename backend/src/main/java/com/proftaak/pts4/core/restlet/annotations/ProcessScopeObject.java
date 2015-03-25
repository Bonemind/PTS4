package com.proftaak.pts4.core.restlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this for methods that need to be called when a scope object is requested
 *
 * This can be used for validation, permissions checks, ets
 *
 * Created by Michon on 12-3-2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessScopeObject {
    /**
     * The class that this is a process method for
     */
    Class value();
}
