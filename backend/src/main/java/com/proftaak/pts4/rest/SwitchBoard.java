package com.proftaak.pts4.rest;

import com.proftaak.pts4.rest.annotations.PreRequest;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michon on 23-4-2015.
 */
public class SwitchBoard extends HttpHandler {
    /**
     * The routes this switchboard handles
     */
    public Map<Pattern, Map<HTTPMethod, Router.Route>> routes = new HashMap<>();

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

        // Cached the patterns
        Map<String, Pattern> patterns = new HashMap<>();

        // Perform routing
        Router router = new Router();
        for (Router.Route route : router.getRoutes()) {
            // Compile the path
            if (!patterns.containsKey(route.pattern)) {
                patterns.put(route.pattern, compilePattern(route.pattern));
            }
            Pattern pattern = patterns.get(route.pattern);

            // Store the route
            if (!routes.containsKey(pattern)) {
                routes.put(pattern, new HashMap<>());
            }
            Map<HTTPMethod, Router.Route> routesForPattern = routes.get(pattern);
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
        // Try to handle the request with a route
        for (Map.Entry<Pattern, Map<HTTPMethod, Router.Route>> routeEntry : this.routes.entrySet()) {
            Matcher matcher = routeEntry.getKey().matcher(request.getRequestURI());
            if (matcher.matches()) {
                // Matching route, see if we have a handler for this method
                for (Map.Entry<HTTPMethod, Router.Route> methodEntry : routeEntry.getValue().entrySet()) {
                    if (getMethod(methodEntry.getKey()) == request.getMethod()) {
                        this.handleRequest(request, response, matcher, methodEntry.getValue().handler);
                        return;
                    }
                }

                // No handler for this method
                this.handleError(response, HTTPException.ERROR_METHOD_NOT_ALLOWED);
            }
        }

        // No matching routes
        this.handleError(response, HTTPException.ERROR_NOT_FOUND);
    }

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
                return null;
        }
    }

    /**
     * Relay an exception back to the client
     *
     * @param request  The request
     * @param response The response
     * @param matcher  The matcher for the current route
     * @param method   The handler for the current route
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
            // Require payload data for some methods.
            if (requestData.getPayload() == null &&
                (request.getMethod() == org.glassfish.grizzly.http.Method.POST ||
                    request.getMethod() == org.glassfish.grizzly.http.Method.PUT)) {
                throw new HTTPException("This method requires a payload");
            }

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
        // The require fields annotation
        /*RequireFields fieldsAnnotation = method.getAnnotation(RequireFields.class);
        if (fieldsAnnotation != null) {
            for (String field : fieldsAnnotation.fields()) {
                if (requestData.getPayload().getOrDefault(field, null) == null) {
                    throw new HTTPException("Missing required parameter: " + field, HttpStatus.BAD_REQUEST_400);
                }
            }
        }*/

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
    private static Object handleOptions(RequestData requestData) {
        return null;
    }
}
