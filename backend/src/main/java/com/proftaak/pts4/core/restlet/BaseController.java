package com.proftaak.pts4.core.restlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proftaak.pts4.core.annotations.RequireAuth;
import com.proftaak.pts4.core.gson.AnnotationExclusionStrategy;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.resource.*;
import org.restlet.util.Series;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
public class BaseController extends ServerResource {
    protected Gson GSON = null;

    /**
     * The key used by restlet for headers.
     */
    private static final String RESTLET_HEADER_KEY = "org.restlet.http.headers";

    /**
     * Some data that may or may not be set by headers, that can be accessed in the routes.
     */
    private User user;
    private Token token;

    public BaseController() {
        GSON = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
    }

    /**
     * Set all applicable cross-origin headers.
     */
    private void setCORS() {
        Response response = this.getResponse();
        Series<Header> responseHeaders = (Series<Header>) response.getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<Header>(Header.class);
            response.getAttributes().put(RESTLET_HEADER_KEY, responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "*");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,X-TOKEN");
    }

    /**
     * Send a response back to the client.
     * @param response The data to send back.
     */
    private void processResponse(Map<String, Object> response) {
        // Set the headers.
        setCORS();

        // Set the body.
        if (response != null) {
            setResponseBody(response);
        }
    }

    /**
     * Relay an exception back to the client.
     * @param exc The exception.
     */
    private void processError(Exception exc) {
        // Set the headers.
        setCORS();

        // Convert the exception, if needed.
        HTTPException userException;
        if (!(exc instanceof HTTPException)) {
            // Print the stacktrace, if useful.
            if (!(exc instanceof NotImplementedException)) {
                exc.printStackTrace();
            }

            // Build an HTTPException.
            userException = new HTTPException(
                "The server encountered an internal error when trying to process your request.",
                Status.SERVER_ERROR_INTERNAL
            );
        } else {
            userException = (HTTPException) exc;
        }

        // Something went wrong, so set the appropriate status code.
        this.setStatus(userException.getStatus());

        // Set the body.
        Map<String, Object> response = new HashMap<>();
        response.put("error", userException.getMessage());
        setResponseBody(response);
    }

    /**
     * Process the annotations of the route that is being accessed.
     */
    private void processAnnotations(Method method) throws FileNotFoundException, SQLException, HTTPException {
        // Get the headers.
        Series<Header> responseHeaders = (Series<Header>) getRequestAttributes().get(RESTLET_HEADER_KEY);

        // Clear the status fields.
        this.user = null;
        this.token = null;

        // The require auth annotation.
        RequireAuth authAnnotation = method.getAnnotation(RequireAuth.class);
        if (authAnnotation != null) {
            // Validate the token.
            String tokenString = responseHeaders.getFirstValue("X-token");
            Token token = Token.getDao().queryForId(tokenString);
            if (token == null || !token.isValid()) {
                throw new HTTPException("You need to pass a valid token to access this route", Status.CLIENT_ERROR_UNAUTHORIZED);
            }

            // Set the status fields.
            this.user = token.getUser();
            this.token = token;
        }
    }

    /**
     * Set the body of the response.
     * @param body The body of the response.
     */
    private void setResponseBody(Object body) {
        getResponse().setEntity(GSON.toJson(body), MediaType.APPLICATION_JSON);
    }

    /**
     * The wrapper methods that receive and decode the requests, and that relay the responses.
     */
    @Get
    public void getWrapper() {
        try {
            Object urlParam = getRequestAttributes().get("id");
            if (urlParam == null) {
                processAnnotations(this.getClass().getMethod("getHandler"));
                processResponse(getHandler());
            } else {
                processAnnotations(this.getClass().getMethod("getHandler", String.class));
                processResponse(getHandler(urlParam.toString()));
            }
        } catch (Exception e) {
            processError(e);
        }
    }
    @Post("json")
    public void postWrapper(String json) {
        try {
            Map<String, Object> data = GSON.fromJson(json, Map.class);
            processAnnotations(this.getClass().getMethod("postHandler", Map.class));
            processResponse(postHandler(data));
        } catch (Exception e) {
            processError(e);
        }
    }
    @Put("json")
    public void putWrapper(String json) {
        try {
            Map<String, Object> data = GSON.fromJson(json, Map.class);
            Object urlParam = getRequestAttributes().get("id");
            processAnnotations(this.getClass().getMethod("putHandler", Map.class, String.class));
            processResponse(putHandler(data, urlParam.toString()));
        } catch (Exception e) {
            processError(e);
        }
    }
    @Delete
    public void deleteWrapper() {
        try {
            Object urlParam = getRequestAttributes().get("id");
            processAnnotations(this.getClass().getMethod("deleteHandler", String.class));
            processResponse(deleteHandler(urlParam.toString()));
        } catch (Exception e) {
            processError(e);
        }
    }

    /**
     * The available options (all).
     */
    @Options
    public void doOptions() {
        setCORS();
    }

    /**
     * The methods that can be implemented by the controllers in order to respond to requests.
     */
    public Map<String, Object> getHandler() throws Exception {
        throw new NotImplementedException();
    }
    public Map<String, Object> getHandler(String urlParam) throws Exception {
        throw new NotImplementedException();
    }
    public Map<String, Object> postHandler(Map<String, Object> data) throws Exception {
        throw new NotImplementedException();
    }
    public Map<String, Object> putHandler(Map<String, Object> data, String urlParam) throws Exception {
        throw new NotImplementedException();
    }
    public Map<String, Object> deleteHandler(String urlParam) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * Get the current user, if any.
     * Will be null if the current route does not require authentication.
     *
     * @return The current user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the current token, if any.
     * Will be null if the current route does not require authentication.
     *
     * @return The current token
     */
    public Token getToken() {
        return token;
    }
}
