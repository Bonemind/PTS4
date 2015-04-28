package com.proftaak.pts4.rest;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michon
 */
public class Router {
    /**
     * The name of the package holding the controllers
     */
    public static final String CONTROLLER_PACKAGE = "com.proftaak.pts4.controllers";

    public static class Route {
        /**
         * The URL pattern
         */
        public final String pattern;

        /**
         * The route annotation associated with this route
         */
        public final com.proftaak.pts4.rest.annotations.Route annotation;

        /**
         * The method to call when this route is accessed
         */
        public final Method handler;

        Route(String pattern, com.proftaak.pts4.rest.annotations.Route annotation, Method handler) {
            this.pattern = pattern;
            this.annotation = annotation;
            this.handler = handler;
        }
    }

    /**
     * The route list
     */
    private List<Route> routes = new ArrayList<>();

    public Router() {
        // Perform routing
        Reflections reflections = new Reflections(Router.CONTROLLER_PACKAGE, new MethodAnnotationsScanner());
        for (Method method : reflections.getMethodsAnnotatedWith(com.proftaak.pts4.rest.annotations.Route.class)) {
            // Check whether the method is static
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new AnnotationTypeMismatchException(method, "@Route can only be used on static methods");
            }

            // Get the annotation
            com.proftaak.pts4.rest.annotations.Route routeAnnotation = method.getAnnotation(com.proftaak.pts4.rest.annotations.Route.class);

            // Determine the path
            String path = "";

            if (routeAnnotation.path().isEmpty()) {
                List<String> parts = new ArrayList<>();

                // Start the route with the controller name
                String controllerName = method.getDeclaringClass().getSimpleName().replace("Controller", "").toLowerCase();
                parts.add(controllerName);

                // Add to the route depending on the annotion route parameter
                switch (routeAnnotation.method()) {
                    case GET_ONE:
                    case PUT:
                    case DELETE:
                        // If one of the methods that take arguments, default to "{id}" as route
                        parts.add("{id}");
                        break;

                    default:
                        // Default to no subroute
                        break;
                }

                // Build the path
                path = "/" + StringUtils.join(parts, '/');
            } else {
                // Get the path from the path parameter of the annotation
                path = routeAnnotation.path();
            }

            // Store the route
            Route route = new Route(path, routeAnnotation, method);
            this.routes.add(route);
        }
    }

    public List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }
}
