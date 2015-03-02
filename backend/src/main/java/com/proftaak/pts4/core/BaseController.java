package com.proftaak.pts4.core;

import com.google.gson.Gson;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.adapter.HttpResponse;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.*;
import org.restlet.util.Series;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
            responseHeaders = new Series(Header.class);
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
    private void processResponse(Object response) {
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
        //} catch (SecurityException e) {
        //} catch (NoSuchMethodException e) {
        //} catch (InvocationTargetException e) {
        //} catch (IllegalAccessException e) {

        // Set the headers.
        setCORS();

        // Something went wrong, so set the appropriate status code.
        this.setStatus(Status.SERVER_ERROR_INTERNAL);

        // Set the body.
        setResponseBody(exc.getMessage());
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
            processResponse(getHandler());
        } catch (Exception e) {
            processError(e);
        }
    }
    @Post("json")
    public void postWrapper(Representation rep) {
        try {
            Object data = GSON.fromJson(rep.getText(), Object.class);
            processResponse(postHandler(data));
        } catch (Exception e) {
            processError(e);
        }
    }
    @Put("json")
    public void putWrapper(Representation rep) {
        try {
            Object data = GSON.fromJson(rep.getText(), Object.class);
            processResponse(putHandler(data));
        } catch (Exception e) {
            processError(e);
        }
    }
    @Delete
    public void deleteWrapper() {
        try {
            processResponse(deleteHandler());
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
    public Object getHandler() throws Exception {
        throw new NotImplementedException();
    }
    public Object postHandler(Object data) throws Exception {
        throw new NotImplementedException();
    }
    public Object putHandler(Object data) throws Exception {
        throw new NotImplementedException();
    }
    public Object deleteHandler() throws Exception {
        throw new NotImplementedException();
    }
}
