package com.proftaak.pts4.core.restlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.dao.Dao;
import com.proftaak.pts4.core.gson.AnnotationExclusionStrategy;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.core.restlet.annotations.ValidateScopeObject;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.resource.*;
import org.restlet.util.Series;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
abstract public class BaseController extends ServerResource {
    protected static Gson GSON = null;

    /**
     * The key used by restlet for headers.
     */
    private static final String RESTLET_HEADER_KEY = "org.restlet.http.headers";

    /**
     * All data relevant for a single handled request.
     */
    protected class RequestData {
        /**
         * The parameter that have been passed in the URL.
         */
        protected Map<String, Object> urlParams;

        /**
         * The payload that has been sent from the client.
         */
        protected Map<String, Object> payload;

        /**
         * The currently logged in user, if any.
         */
        protected User user;

        /**
         * The currently used token, if any.
         */
        protected Token token;

        public Map<String, Object> getUrlParams() {
            return urlParams;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public User getUser() {
            return user;
        }

        public Token getToken() {
            return token;
        }

        /**
         * Get the object of the given type from the current request.
         * <p>
         * This object has to be available in the URL, and the type must be either the type of this controller, or of one of the parent controllers.
         *
         * @param cls The type of object to get.
         * @return The object, if it was found.
         * @throws HTTPException If the object cannot be found. This probably means the id in the URL is invalid, so let this propagate.
         */
        public <O> O getScopeObject(Class<O> cls) throws Exception {
            // Get the object.
            O obj = this.getScopeObjectFromController(cls, BaseController.this.getClass());

            // Validate the object.
            boolean isValid = obj != null && this.validateScopeObjectWithController(obj, BaseController.this.getClass());

            // If the object is not valid, throw an error.
            if (!isValid) {
                throw new HTTPException("That " + cls.getSimpleName().toLowerCase() + " does not exist", Status.CLIENT_ERROR_NOT_FOUND);
            }

            return obj;
        }

        private <O> O getScopeObjectFromController(Class<O> cls, Class<? extends BaseController> controllerCls) throws Exception {
            // Get the annotation.
            CRUDController crudAnnotation = controllerCls.getAnnotation(CRUDController.class);
            if (crudAnnotation == null) {
                throw new Exception("The controller needs to be a CRUD controller in order for this function to work.");
            }

            // Check whether the controller can provide the requested object.
            if (crudAnnotation.table() == cls) {
                // Get the object from the database.
                String name = cls.getSimpleName().toLowerCase();
                Dao<O, Integer> dao = (Dao) cls.getMethod("getDao").invoke(cls);
                int id = Integer.parseInt(getUrlParams().get(name + "Id").toString());
                return dao.queryForId(id);
            } else if (crudAnnotation.parent() != null) {
                // Recurse into the parent, if there is one.
                return this.getScopeObjectFromController(cls, crudAnnotation.parent());
            } else {
                // Requested object class cannot be provided.
                throw new Exception("Unable to get scope object of type " + cls.getSimpleName());
            }
        }

        private <O> boolean validateScopeObjectWithController(O obj, Class<? extends BaseController> controllerCls) throws Exception {
            // Let the controller process the object.
            try {
                Reflections r = new Reflections(ClasspathHelper.forClass(controllerCls), new MethodAnnotationsScanner());
                for (Method processor : r.getMethodsAnnotatedWith(ValidateScopeObject.class)) {
                    if (processor.getAnnotation(ValidateScopeObject.class).value() == obj.getClass()) {
                        if (!(boolean) processor.invoke(null, this, obj)) {
                            return false;
                        }
                    }
                }
            } catch (InvocationTargetException e) {
                throw (Exception) e.getCause();
            }

            // If the controller has a parent, let that validate the object too.
            CRUDController crudAnnotation = controllerCls.getAnnotation(CRUDController.class);
            if (crudAnnotation != null && crudAnnotation.parent() != null) {
                return this.validateScopeObjectWithController(obj, crudAnnotation.parent());
            }

            // None of the checks failed, so the object is valid.
            return true;
        }

        public String toString() {
            return "User: " + user + "\n" +
                    "Token: " + token + "\n" +
                    "Data: " + GSON.toJson(getPayload()) + "\n" +
                    "Params: " + GSON.toJson(getUrlParams());
        }

        protected RequestData() {
        }
    }

    public static void init() {
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
        responseHeaders.add("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,X-TOKEN");
    }

    /**
     * Build a RequestData object for the current request.
     *
     * @param payload The payload, if any.
     * @return The RequestData object for this request.
     */
    private RequestData buildRequest(String payload) throws FileNotFoundException, SQLException {
        RequestData data = new RequestData();

        // Get the url parameters.
        data.urlParams = getRequestAttributes();

        // Get the payload, if any.
        if (payload != null) {
            data.payload = GSON.fromJson(payload, Map.class);
        }

        // Get the token/user, if any.
        Series<Header> responseHeaders = (Series<Header>) getRequestAttributes().get(RESTLET_HEADER_KEY);
        String tokenString = responseHeaders.getFirstValue("X-token");
        Token token = Token.getDao().queryForId(tokenString);
        if (token != null && token.isValid()) {
            data.token = token;
            data.user = token.getUser();
        }

        return data;
    }

    /**
     * Send a response back to the client.
     *
     * @param response The data to send back.
     */
    private void processResponse(Object response) {
        // Set the headers.
        setCORS();

        // Set the body.
        if (response != null) {
            setResponseBody(response);
        }
    }

    /**
     * Relay an exception back to the client.
     *
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
     * Set the body of the response.
     *
     * @param body The body of the response.
     */
    private void setResponseBody(Object body) {
        getResponse().setEntity(GSON.toJson(body), MediaType.APPLICATION_JSON);
    }

    /**
     * Process the annotations of the route that is being accessed.
     */
    private void processAnnotations(Method method, RequestData requestData) throws FileNotFoundException, SQLException, HTTPException {
        // The require auth annotation.
        RequireAuth authAnnotation = method.getAnnotation(RequireAuth.class);
        if (authAnnotation != null) {
            // Check whether the user is logged in.
            if (requestData.token == null) {
                throw new HTTPException("You need to pass a valid token to access this route", Status.CLIENT_ERROR_UNAUTHORIZED);
            }

            // If present, verify the roles field.
            if (authAnnotation.roles().length > 0) {
                requestData.getUser().getRole().require(authAnnotation.roles());
            }
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
     * The wrapper methods that receive and decode the requests, and that relay the responses.
     */

    @Get
    public void getWrapper() {
        try {
            RequestData requestData = this.buildRequest(null);
            processAnnotations(this.getClass().getMethod("getHandler", RequestData.class), requestData);
            processResponse(getHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    @Post("json")
    public void postWrapper(String json) {
        try {
            RequestData requestData = this.buildRequest(json);
            processAnnotations(this.getClass().getMethod("postHandler", RequestData.class), requestData);
            processResponse(postHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    @Put("json")
    public void putWrapper(String json) {
        try {
            RequestData requestData = this.buildRequest(json);
            processAnnotations(this.getClass().getMethod("putHandler", RequestData.class), requestData);
            processResponse(putHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    @Delete
    public void deleteWrapper() {
        try {
            RequestData requestData = this.buildRequest(null);
            processAnnotations(this.getClass().getMethod("deleteHandler", RequestData.class), requestData);
            processResponse(deleteHandler(requestData));
        } catch (Exception e) {
            processError(e);
        }
    }

    /**
     * Handle a GET request.
     *
     * @param requestData The request data.
     * @return The result of the request. This will be converted to JSON and served to the requestee.
     */
    public Object getHandler(RequestData requestData) throws Exception {
        throw new HTTPException("Method not allowed", Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * Handle a POST request.
     *
     * @param requestData The request data.
     * @return The result of the request. This will be converted to JSON and served to the requestee.
     */
    public Object postHandler(RequestData requestData) throws Exception {
        throw new HTTPException("Method not allowed", Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * Handle a PUT request.
     *
     * @param requestData The request data.
     * @return The result of the request. This will be converted to JSON and served to the requestee.
     */
    public Object putHandler(RequestData requestData) throws Exception {
        throw new HTTPException("Method not allowed", Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * Handle a DELETE request.
     *
     * @param requestData The request data.
     * @return The result of the request. This will be converted to JSON and served to the requestee.
     */
    public Object deleteHandler(RequestData requestData) throws Exception {
        throw new HTTPException("Method not allowed", Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }
}
