package com.proftaak.pts4.core.restlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Michon on 12-3-2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValidateScopeObject {
    /**
     * The class that this is a validation method for.
     */
    Class value();
}
