package com.proftaak.pts4.core.rest;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Token;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.apache.commons.lang3.ObjectUtils;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import javax.naming.PartialResultException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michon
 */
public class Router extends HttpHandler {
    /**
     * The name of the package holding the controllers
     */
    public static final String CONTROLLER_PACKAGE = "com.proftaak.pts4.controllers";

    private class Route {
        public final Pattern pattern;
        public final com.proftaak.pts4.core.rest.annotations.Route route;
        public final Method method;

        Route(Pattern pattern, com.proftaak.pts4.core.rest.annotations.Route route, Method method) {
            this.pattern = pattern;
            this.route = route;
            this.method = method;
        }
    }

    /**
     * The route list
     */
    private List<Route> routes = new ArrayList<>();

    public Router() {
        // Perform routing
        Reflections reflections = new Reflections(Router.CONTROLLER_PACKAGE, new MethodAnnotationsScanner());
        for (Method method : reflections.getMethodsAnnotatedWith(com.proftaak.pts4.core.rest.annotations.Route.class)) {
            // Check whether the method is static
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new AnnotationTypeMismatchException(method, "@Route can only be used on static methods");
            }

            // Get the annotation
            com.proftaak.pts4.core.rest.annotations.Route route = method.getAnnotation(com.proftaak.pts4.core.rest.annotations.Route.class);

            // Determine the route
            List<String> path = new ArrayList<>();

            if (route.route().isEmpty()) {
                // Start the route with the controller name
                String controllerName = method.getDeclaringClass().getSimpleName().replace("Controller", "").toLowerCase();
                path.add(controllerName);

                // Add to the route depending on the annotion route parameter
                switch (route.method()) {
                    case GET_ONE:
                    case PUT:
                    case DELETE:
                        // If one of the methods that take arguments, default to "{id}" as route
                        path.add("{id}");
                        break;

                    default:
                        // Default to no subroute
                        break;
                }
            } else {
                // Build the route from the route parameter of the annotion
                path.addAll(Arrays.asList(org.apache.commons.lang3.StringUtils.split(route.route(), '/')));
            }

            // Convert the route to a regex
            String routePatternString = "^";
            for (String part : path) {
                routePatternString += "/";
                if (part.startsWith("{") && part.endsWith("}")) {
                    part = part.substring(1, part.length() - 1);
                    routePatternString += "(?<" + part + ">[^/]+)";
                } else {
                    routePatternString += part.toLowerCase();
                }
            }
            routePatternString += "/?$";
            Pattern routePattern = Pattern.compile(routePatternString, Pattern.CASE_INSENSITIVE);

            // Store the route
            this.routes.add(new Route(routePattern, route, method));
        }
    }

    /**
     * Process a HTTP request
     *
     * @param request The request
     * @param response The response
     */
    public void service(Request request, Response response) {
        // Try to handle the request with a route
        boolean hasRoute = false;
        for (Route route : this.routes) {
            Matcher matcher = route.pattern.matcher(request.getRequestURI());
            if (matcher.matches()) {
                hasRoute = true;
                if (route.route.method().method == request.getMethod()) {
                    handleRequest(request, response, matcher, route.method);
                    return;
                }
            }
        }

        // No matching route, return error
        if (hasRoute) {
            this.handleError(response, HTTPException.ERROR_METHOD_NOT_ALLOWED);
        } else {
            this.handleError(response, HTTPException.ERROR_NOT_FOUND);
        }
    }

    /**
     * Relay an exception back to the client
     *
     * @param request The request
     * @param response The response
     * @param matcher The matcher for the current route
     * @param method The handler for the current route
     */
    private void handleRequest(Request request, Response response, Matcher matcher, Method method) {
        // Set the CORS headers
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-TOKEN");

        // Build the request
        RequestData requestData;
        try {
            requestData = RequestData.buildRequest(request, matcher);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Object responseObject = null;
        try {
            // Call the pre-request methods
            this.handlePrerequests(method, requestData);

            // Handle the annotations for the current route
            this.handleAnnotations(method, requestData);

            // Call the handling method
            try {
                responseObject = method.invoke(null, requestData);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        } catch (Throwable throwable) {
            responseObject = this.handleError(response, throwable);
        }

        // Serialize the response object, if any
        String responseBody = null;
        if (responseObject != null) {
            responseBody = requestData.jsonSerializer.serialize(responseObject);
        }

        // Build the response
        response.setContentType("application/json");
        if (responseBody == null || responseBody.length() == 0) {
            response.setContentLength(0);
            if (response.getStatus() == HttpStatus.OK_200.getStatusCode()) {
                response.setStatus(HttpStatus.NO_CONTENT_204);
            }
        } else {
            response.setContentLength(responseBody.length());
            try {
                response.getWriter().write(responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle an exception, returning information about what went wrong
     *
     * @param response The response to configure
     * @param throwable The throwable that occurred
     */
    private Object handleError(Response response, Throwable throwable) {
        HTTPException userException;
        if (throwable instanceof HTTPException) {
            // Convert the exception, if possible
            userException = (HTTPException) throwable;
        } else {
            // Output the error.
            throwable.printStackTrace();

            // Default to a generic HTTPException
            userException = HTTPException.ERROR_BAD_REQUEST;
        }

        // Something went wrong, so set the appropriate status code
        response.setStatus(userException.getStatus());

        // Create a simple object containing only the error message
        Map<String, Object> responseObject = new HashMap<>();
        responseObject.put("error", userException.getMessage());
        return responseObject;
    }

    /**
     * Process the annotations of the route that is being accessed
     *
     * @param method The method to process the annotations of
     * @param requestData The data for the current request
     */
    private void handleAnnotations(Method method, RequestData requestData) throws HTTPException {
        // The require auth annotation
        RequireAuth authAnnotation = method.getAnnotation(RequireAuth.class);
        if (authAnnotation != null) {
            // Check whether the user is logged in
            if (requestData.token == null) {
                throw new HTTPException("You need to pass a valid token to access this route", HttpStatus.UNAUTHORIZED_401);
            }

            // Verify the roles field
            requestData.requireScopeRole(authAnnotation.role());
        }
    }

    /**
     * Call all pre-request methods in the current controller
     *
     * @param method The method to call the pre-request methods for
     * @param requestData The data for the current request
     */
    private void handlePrerequests(Method method, RequestData requestData) throws Exception {
        // Call all pre-request methods from the controller in which the method lives
        try {
            Class cls = method.getDeclaringClass();
            Reflections r = new Reflections(ClasspathHelper.forClass(cls), new MethodAnnotationsScanner());
            for (Method processor : r.getMethodsAnnotatedWith(PreRequest.class)) {
                if (processor.getDeclaringClass() == cls) {
                    processor.invoke(null, requestData);
                }
            }
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
