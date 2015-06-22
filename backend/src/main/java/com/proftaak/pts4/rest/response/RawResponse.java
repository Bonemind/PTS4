package com.proftaak.pts4.rest.response;

import org.glassfish.grizzly.http.server.Response;

/**
 * Created by Michon on 18-6-2015.
 */
public class RawResponse extends BaseResponse {
    private String body;
    private String contentType;

    public RawResponse(String body, String contentType) {
        this.body = body;
        this.contentType = contentType;
    }

    @Override
    public void prepareResponse(Response response) {
        BaseResponse.setBody(response, this.body, this.contentType);
    }
}
