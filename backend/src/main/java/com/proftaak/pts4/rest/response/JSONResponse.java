package com.proftaak.pts4.rest.response;

import com.proftaak.pts4.json.JSONSerializerFactory;
import flexjson.JSONSerializer;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Map;

/**
 * Created by Michon on 17-6-2015.
 */
public class JSONResponse<T> extends BaseResponse {
    /**
     * The response data
     */
    private T response;

    /**
     * The HTTP status
     */
    private HttpStatus status = HttpStatus.OK_200;

    /**
     * The JSON serializer
     */
    private JSONSerializer jsonSerializer;

    public JSONResponse() {
        this.jsonSerializer = JSONSerializerFactory.createSerializer();
    }

    public JSONResponse(T response) {
        this();
        this.response = response;
    }

    public JSONResponse(T response, HttpStatus status) {
        this(response);
        this.status = status;
    }

    public T getResponse() {
        return this.response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    @Override
    public void prepareResponse(Response response) {
        // Convert metadata to map, add data
        Map<String, Object> data = this.getMetadata().getData();
        data.put("data", this.response);

        // Convert to JSON
        this.jsonSerializer.include("data");
        String body = this.jsonSerializer.serialize(data);

        // Set response
        BaseResponse.setBody(response, body, "application/json");
        response.setStatus(this.status);
    }
}
