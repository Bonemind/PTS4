package com.proftaak.pts4.rest.response;

import com.proftaak.pts4.rest.response.metadata.Metadata;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;

/**
 * Created by Michon on 18-6-2015.
 */
public abstract class BaseResponse {
    /**
     * The metadata of the response
     */
    private Metadata metadata = new Metadata();

    /**
     * Prepare the response, setting all relevant settings
     */
    abstract public void prepareResponse(Response response);

    public Metadata getMetadata() {
        return this.metadata;
    }

    /**
     * Set the body and content type of the response
     *
     * @param response    The response to set
     * @param body        The body of the response
     * @param contentType The content type of the body
     */
    protected static void setBody(Response response, String body, String contentType) {
        response.setContentType(contentType);
        if (body == null || body.length() == 0) {
            response.setContentLength(0);
            if (response.getStatus() == HttpStatus.OK_200.getStatusCode()) {
                response.setStatus(HttpStatus.NO_CONTENT_204);
            }
        } else {
            response.setContentLength(body.length());
            try {
                response.getWriter().write(body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
