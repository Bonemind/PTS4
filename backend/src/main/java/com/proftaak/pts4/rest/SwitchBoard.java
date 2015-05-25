package com.proftaak.pts4.rest;

import com.proftaak.pts4.json.JSONSerializerFactory;
import com.proftaak.pts4.rest.annotations.Field;
import com.proftaak.pts4.rest.annotations.Fields;
import com.proftaak.pts4.rest.annotations.PreRequest;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import flexjson.JSONSerializer;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michon on 23-4-2015.
 */
public class SwitchBoard extends HttpHandler {
    /**
     * Precompiled patterns
     */
    private Map<String, Pattern> patterns = new HashMap<>();

    /**
     * The routes this switchboard handles
     */
    private Map<Pattern, Map<HTTPMethod, Router.Route>> routes = new HashMap<>();

    public SwitchBoard() {
        // Get the option route.
        Method optionsMethod = null;
        com.proftaak.pts4.rest.annotations.Route optionsRoute = null;
        try {
            optionsMethod = this.getClass().getDeclaredMethod("handleOptions", RequestData.class);
            optionsRoute = optionsMethod.getAnnotation(com.proftaak.pts4.rest.annotations.Route.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // Perform routing
        Router router = new Router();
        for (Router.Route route : router.getRoutes()) {
            // Compile the path
            if (!this.patterns.containsKey(route.pattern)) {
                this.patterns.put(route.pattern, compilePattern(route.pattern));
            }
            Pattern pattern = this.patterns.get(route.pattern);

            // Store the route
            if (!this.routes.containsKey(pattern)) {
                this.routes.put(pattern, new HashMap<>());
            }
            Map<HTTPMethod, Router.Route> routesForPattern = this.routes.get(pattern);
            if (routesForPattern.containsKey(route.annotation.method())) {
                throw new KeyAlreadyExistsException("There already is a route with that path and method");
            }
            routesForPattern.put(route.annotation.method(), route);

            // If there is no OPTION handler for this route, add it.
            if (!routesForPattern.containsKey(HTTPMethod.OPTIONS)) {
                routesForPattern.put(HTTPMethod.OPTIONS, new Router.Route(route.pattern, optionsRoute, optionsMethod));
            }
        }
    }

    /**
     * Convert a route pattern (/path/{placeholder}/path) to a regex that matches urls.
     *
     * @param urlPattern The pattern
     * @return The regex
     */
    private static Pattern compilePattern(String urlPattern) {
        String routePatternString = "^";
        for (String part : StringUtils.split(urlPattern, '/')) {
            routePatternString += "/";
            if (part.startsWith("{") && part.endsWith("}")) {
                part = part.substring(1, part.length() - 1);
                routePatternString += "(?<" + part + ">[^/]+)";
            } else {
                routePatternString += part.toLowerCase();
            }
        }
        routePatternString += "/?$";
        return Pattern.compile(routePatternString, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Process a HTTP request
     *
     * @param request  The request
     * @param response The response
     */
    public void service(Request request, Response response) {
        try {
            // Try to handle the request with a route
            for (Map.Entry<Pattern, Map<HTTPMethod, Router.Route>> routeEntry : this.routes.entrySet()) {
                Matcher matcher = routeEntry.getKey().matcher(request.getRequestURI());
                if (matcher.matches()) {
                    // Matching route, see if we have a handler for this method
                    for (Map.Entry<HTTPMethod, Router.Route> methodEntry : routeEntry.getValue().entrySet()) {
                        if (getMethod(methodEntry.getKey()) == request.getMethod()) {
                            this.handleRequest(request, response, matcher, methodEntry.getValue());
                            return;
                        }
                    }

                    // No handler for this method
                    this.handleError(response, HTTPException.ERROR_METHOD_NOT_ALLOWED);
                }
            }

            // No matching routes
            this.handleError(response, HTTPException.ERROR_NOT_FOUND);
        } catch (Throwable t) {
            t.printStackTrace();
            this.handleError(response, HTTPException.ERROR_INTERNAL);
        }
    }

    /**
     * Convert a HTTPMethod to an org.glassfish.grizzly.http.Method
     */
    private org.glassfish.grizzly.http.Method getMethod(HTTPMethod method) {
        switch (method) {
            case GET:
            case GET_ONE:
                return org.glassfish.grizzly.http.Method.GET;
            case POST:
                return org.glassfish.grizzly.http.Method.POST;
            case PUT:
                return org.glassfish.grizzly.http.Method.PUT;
            case DELETE:
                return org.glassfish.grizzly.http.Method.DELETE;
            case OPTIONS:
                return org.glassfish.grizzly.http.Method.OPTIONS;
            default:
                return org.glassfish.grizzly.http.Method.CUSTOM("UNKNOWN");
        }
    }

    /**
     * Relay an exception back to the client
     *
     * @param request  The request
     * @param response The response
     * @param matcher  The matcher for the current route
     * @param route    The current route
     */
    private void handleRequest(Request request, Response response, Matcher matcher, Router.Route route) {
        // Set the CORS headers
        response.addHeader("Access-Control-Allow-Origin", "*");
        Collection<String> methods = new TreeSet<>();
        for (HTTPMethod method : this.routes.getOrDefault(this.patterns.get(route.pattern), new HashMap<>()).keySet()) {
            methods.add(getMethod(method).getMethodString());
        }
        response.addHeader("Access-Control-Allow-Methods", StringUtils.join(methods, ","));
        response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-TOKEN");

        Object responseObject = null;
        try {
            // Build the request
            RequestData requestData = RequestData.buildRequest(request, matcher);

            // Call the pre-request methods
            this.handlePrerequests(route.handler, requestData);

            // Handle the annotations for the current route
            this.handleAnnotations(route.handler, requestData);

            // Call the handling method
            try {
                responseObject = route.handler.invoke(null, requestData);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        } catch (Throwable throwable) {
            responseObject = this.handleError(response, throwable);
        }

        // Serialize the response object, if any
        String responseBody = null;
        if (responseObject != null) {
            // Create a new JSON serializer.
            JSONSerializer jsonSerializer = JSONSerializerFactory.createSerializer();

            // Serialize the response
            responseBody = jsonSerializer.serialize(responseObject);
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
     * @param response  The response to configure
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
     * @param method      The method to process the annotations of
     * @param requestData The data for the current request
     */
    private void handleAnnotations(Method method, RequestData requestData) throws HTTPException {
        // The (so far) unknown fields that are present in the payload
        Set<String> unknownKeys = new TreeSet<>();
        if (requestData.getPayload() != null) {
            unknownKeys.addAll(requestData.getPayload().keySet());
        }

        // The field annotations
        Fields fieldsAnnotation = method.getAnnotation(Fields.class);
        if (fieldsAnnotation != null) {
            for (Field field : fieldsAnnotation.value()) {
                // If the field is not set, and it is required, error
                if (requestData.getPayload().getOrDefault(field.name(), null) == null && field.required()) {
                    throw new HTTPException("Missing required parameter: " + field.name(), HttpStatus.BAD_REQUEST_400);
                }

                // Remove the field from the unknown keys
                unknownKeys.remove(field.name());
            }
        }

        // If any items remain in the unknown keys, those are actually unknown, so error
        /*if (unknownKeys.size() > 0) {
            throw new HTTPException("Unknown parameter: " + unknownKeys.toArray()[0], HttpStatus.BAD_REQUEST_400);
        }*/

        // The require auth annotation
        RequireAuth authAnnotation = method.getAnnotation(RequireAuth.class);
        if (authAnnotation != null) {
            // Check whether the user is logged in
            if (requestData.getToken() == null) {
                throw new HTTPException("You need to pass a valid token to access this route", HttpStatus.UNAUTHORIZED_401);
            }

            // Verify the roles field
            requestData.requireScopeRole(authAnnotation.role());
        }
    }

    /**
     * Call all pre-request methods in the current controller
     *
     * @param method      The method to call the pre-request methods for
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

    /**
     * Handle OPTIONS requests.
     */
    @com.proftaak.pts4.rest.annotations.Route(method = com.proftaak.pts4.rest.HTTPMethod.OPTIONS)
    private static void handleOptions(RequestData requestData) {
    }
}
