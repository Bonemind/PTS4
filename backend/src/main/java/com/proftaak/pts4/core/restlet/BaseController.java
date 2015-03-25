package com.proftaak.pts4.core.restlet;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.PreRequest;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Token;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.resource.*;
import org.restlet.util.Series;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015
 */
abstract public class BaseController extends ServerResource {
    /**
     * The key used by restlet for headers
     */
    private static final String RESTLET_HEADER_KEY = "org.restlet.http.headers";

    /**
     * Set all applicable cross-origin headers
     */
    private void setCORS() {
        Response response = this.getResponse();
        Series<Header> responseHeaders = (Series<Header>) response.getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<Header>(Header.class);
            response.getAttributes().put(RESTLET_HEADER_KEY, responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,X-TOKEN");
    }

    /**
     * Send a response back to the client
     *
     * @param response The data to send back
     */
    private void processResponse(RequestData requestData, Object response) {
        // Set the headers
        setCORS();

        // Set the body
        if (response != null) {
            setResponseBody(response, requestData.getSerializer());
        }
    }

    /**
     * Relay an exception back to the client
     *
     * @param exc The exception
     */
    private void processError(Exception exc) {
        // Set the headers
        setCORS();

        // Convert the exception, if needed
        HTTPException userException;
        if (!(exc instanceof HTTPException)) {
            // Print the stacktrace, if useful
            exc.printStackTrace();

            // Build an HTTPException
            userException = new HTTPException(
                    "The server encountered an internal error when trying to process your request.",
                    Status.SERVER_ERROR_INTERNAL
            );
        } else {
            userException = (HTTPException) exc;
        }

        // Something went wrong, so set the appropriate status code
        this.setStatus(userException.getStatus());

        // Set the body
        Map<String, Object> response = new HashMap<>();
        response.put("error", userException.getMessage());
        setResponseBody(response);
    }

    /**
     * Set the body of the response
     *
     * @param body The body of the response
     */
    private void setResponseBody(Object body) {
        this.setResponseBody(body, new JSONSerializer());
    }

    /**
     * Set the body of the response
     *
     * @param body       The body of the response
     * @param serializer The JSONSerializer to use for the serialization
     */
    private void setResponseBody(Object body, JSONSerializer serializer) {
        getResponse().setEntity(serializer.serialize(body), MediaType.APPLICATION_JSON);
    }

    /**
     * Build a RequestData object for the current request
     *
     * @param payload The payload, if any
     * @return The RequestData object for this request
     */
    private RequestData buildRequest(String payload) throws FileNotFoundException {
        RequestData data = new RequestData(this);

        // Get the url parameters
        data.urlParams = getRequestAttributes();

        // Get the payload, if any
        if (payload != null) {
            JSONDeserializer<Map> deserializer = new JSONDeserializer<>();
            data.payload = deserializer.deserialize(payload);
        }

        // Get the token/user, if any
        Series<Header> responseHeaders = (Series<Header>) getRequestAttributes().get(RESTLET_HEADER_KEY);
        String tokenString = responseHeaders.getFirstValue("X-token");
        Token token = tokenString == null ? null : Ebean.find(Token.class, tokenString);
        if (token != null && token.isValid()) {
            data.token = token;
            data.user = token.getUser();
            data.addScopeRole(ScopeRole.USER);
        }

        return data;
    }

    /**
     * Process the annotations of the route that is being accessed
     */
    private void processAnnotations(Method method, RequestData requestData) throws FileNotFoundException, HTTPException {
        // The require auth annotation
        RequireAuth authAnnotation = method.getAnnotation(RequireAuth.class);
        if (authAnnotation != null) {
            // Check whether the user is logged in
            if (requestData.token == null) {
                throw new HTTPException("You need to pass a valid token to access this route", Status.CLIENT_ERROR_UNAUTHORIZED);
            }

            // Verify the roles field
            if (!requestData.hasScopeRole(authAnnotation.role())) {
                throw HTTPException.ERROR_FORBIDDEN;
            }
        }
    }

    /**
     * Call all methods that have been tagged as needing to be called before requests
     */
    private void preRequestForController(RequestData requestData, Class<? extends BaseController> controllerCls) throws Exception {
        // Let the controller process the object
        try {
            Reflections r = new Reflections(ClasspathHelper.forClass(controllerCls), new MethodAnnotationsScanner());
            for (Method processor : r.getMethodsAnnotatedWith(PreRequest.class)) {
                if (processor.getDeclaringClass() == controllerCls) {
                    processor.invoke(null, requestData);
                }
            }
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }

        // If the controller has a parent, let that process the object too
        CRUDController crudAnnotation = controllerCls.getAnnotation(CRUDController.class);
        if (crudAnnotation != null && crudAnnotation.parent() != null) {
            this.preRequestForController(requestData, crudAnnotation.parent());
        }
    }

    /**
     * The available options (all)
     */
    @Options
    public void doOptions() {
        setCORS();
    }

    /**
     * The wrapper methods that receive and decode the requests, and that relay the responses
     */

    @Get
    public void getWrapper() {
        try {
            RequestData requestData = this.buildRequest(null);
            preRequestForController(requestData, this.getClass());
            processAnnotations(this.getClass().getMethod("getHandler", RequestData.class), requestData);
            processResponse(requestData, getHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    @Post("json")
    public void postWrapper(String json) {
        try {
            RequestData requestData = this.buildRequest(json);
            preRequestForController(requestData, this.getClass());
            processAnnotations(this.getClass().getMethod("postHandler", RequestData.class), requestData);
            processResponse(requestData, postHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    @Put("json")
    public void putWrapper(String json) {
        try {
            RequestData requestData = this.buildRequest(json);
            preRequestForController(requestData, this.getClass());
            processAnnotations(this.getClass().getMethod("putHandler", RequestData.class), requestData);
            processResponse(requestData, putHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    @Delete
    public void deleteWrapper() {
        try {
            RequestData requestData = this.buildRequest(null);
            preRequestForController(requestData, this.getClass());
            processAnnotations(this.getClass().getMethod("deleteHandler", RequestData.class), requestData);
            processResponse(requestData, deleteHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    /**
     * Handle a GET request
     *
     * @param requestData The request data
     * @return The result of the request. This will be converted to JSON and served to the requestee
     */
    public Object getHandler(RequestData requestData) throws Exception {
        throw HTTPException.ERROR_METHOD_NOT_ALLOWED;
    }

    /**
     * Handle a POST request
     *
     * @param requestData The request data
     * @return The result of the request. This will be converted to JSON and served to the requestee
     */
    public Object postHandler(RequestData requestData) throws Exception {
        throw HTTPException.ERROR_METHOD_NOT_ALLOWED;
    }

    /**
     * Handle a PUT request
     *
     * @param requestData The request data
     * @return The result of the request. This will be converted to JSON and served to the requestee
     */
    public Object putHandler(RequestData requestData) throws Exception {
        throw HTTPException.ERROR_METHOD_NOT_ALLOWED;
    }

    /**
     * Handle a DELETE request
     *
     * @param requestData The request data
     * @return The result of the request. This will be converted to JSON and served to the requestee
     */
    public Object deleteHandler(RequestData requestData) throws Exception {
        throw HTTPException.ERROR_METHOD_NOT_ALLOWED;
    }
}
