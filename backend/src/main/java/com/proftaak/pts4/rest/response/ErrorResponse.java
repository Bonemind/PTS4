package com.proftaak.pts4.rest.response;

import com.proftaak.pts4.json.JSONSerializerFactory;
import com.proftaak.pts4.rest.HTTPException;
import flexjson.JSONSerializer;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 23-6-2015.
 */
public class ErrorResponse extends BaseResponse {
    /**
     * The exception that caused this error response
     */
    private HTTPException exception;

    public ErrorResponse(HTTPException exception) {
        this.exception = exception;
    }

    @Override
    public void prepareResponse(Response response) {
        // Something went wrong, so set the appropriate status code
        response.setStatus(this.exception.getStatus());

        // Create a simple object containing only the error message
        Map<String, Object> responseObject = new HashMap<>();
        responseObject.put("error", this.exception.getMessage());

        // Serialize the body
        JSONSerializer jsonSerializer = JSONSerializerFactory.createSerializer();
        String body = jsonSerializer.serialize(responseObject);

        // Set the body
        BaseResponse.setBody(response, body, "application/json");
    }
}
