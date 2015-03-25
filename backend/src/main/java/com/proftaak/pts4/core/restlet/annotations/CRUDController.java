package com.proftaak.pts4.core.restlet.annotations;

import com.proftaak.pts4.core.restlet.BaseController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this for controllers that function as a CRUD controller for a given table class
 *
 * @author Michon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CRUDController {
    /**
     * The table class
     */
    Class table();

    /**
     * The parent controller
     */
    Class<? extends BaseController> parent() default BaseController.class;
}
