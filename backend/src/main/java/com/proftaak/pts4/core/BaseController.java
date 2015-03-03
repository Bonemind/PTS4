package com.proftaak.pts4.core;

import com.google.gson.Gson;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.resource.*;
import org.restlet.util.Series;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
public class BaseController extends ServerResource {
    protected Gson GSON = new Gson();

    /**
     * Set all applicable cross-origin headers.
     */
    private void setCORS() {
        Response response = this.getResponse();
        Series<Header> responseHeaders = (Series<Header>) response.getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<Header>(Header.class);
            response.getAttributes().put("org.restlet.http.headers", responseHeaders);
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
        setResponseBody(response);
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
                processResponse(getHandler());
            } else {
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
            processResponse(putHandler(data, urlParam.toString()));
        } catch (Exception e) {
            processError(e);
        }
    }
    @Delete
    public void deleteWrapper() {
        try {
            Object urlParam = getRequestAttributes().get("id");
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
}
