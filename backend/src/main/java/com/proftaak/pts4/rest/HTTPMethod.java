package com.proftaak.pts4.rest;

/**
 * Created by Michon on 23-4-2015.
 */
public enum HTTPMethod {
    /**
     * GET /stuff
     */
    GET("GET"),

    /**
     * GET /stuff/{id}
     */
    GET_ONE("GET"),

    /**
     * POST /stuff
     */
    POST("POST"),

    /**
     * PUT /stuff/{id}
     */
    PUT("PUT"),

    /**
     * DELETE /stuff/{id}
     */
    DELETE("DELETE"),

    /**
     * OPTIONS /stuff or /stuff/{id}
     */
    OPTIONS("OPTIONS");

    public final String method;

    HTTPMethod(String method) {
        this.method = method;
    }
}
