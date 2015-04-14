package com.proftaak.pts4.core.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Michon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    public enum Method {
        /**
         * GET /stuff
         */
        GET(org.glassfish.grizzly.http.Method.GET),

        /**
         * GET /stuff/{id}
         */
        GET_ONE(org.glassfish.grizzly.http.Method.GET),

        /**
         * POST /stuff
         */
        POST(org.glassfish.grizzly.http.Method.POST),

        /**
         * PUT /stuff/{id}
         */
        PUT(org.glassfish.grizzly.http.Method.PUT),

        /**
         * DELETE /stuff/{id}
         */
        DELETE(org.glassfish.grizzly.http.Method.DELETE),

        /**
         * OPTIONS /stuff or /stuff/{id}
         */
        OPTIONS(org.glassfish.grizzly.http.Method.OPTIONS);

        public final org.glassfish.grizzly.http.Method method;

        Method(org.glassfish.grizzly.http.Method method) {
            this.method = method;
        }
    }

    /**
     * The method this route should respond to
     */
    Method method();

    /**
     * The route url
     */
    String route() default "";
}
