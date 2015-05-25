package com.proftaak.pts4.rest.annotations;

import java.lang.annotation.*;

/**
 * Indicates which fields a route accepts/requires
 *
 * Can only be used in conjunction with @Route
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Fields.class)
public @interface Field {
    /**
     * The name of the field
     */
    String name();

    /**
     * Whether the field is required
     */
    boolean required() default false;

    /**
     * The description of the field
     */
    String description();

    /**
     * The type of the field
     */
    Class type() default String.class;
}