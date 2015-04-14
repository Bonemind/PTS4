package com.proftaak.pts4.rest;

import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * @author Michon
 */
public class HTTPException extends Exception {
    /**
     * The user does not have permission to do what he wants to do
     */
    public static final HTTPException ERROR_FORBIDDEN = new HTTPException("You do not have permission to do that", HttpStatus.FORBIDDEN_403);

    /**
     * The request is somehow invalid
     */
    public static final HTTPException ERROR_BAD_REQUEST = new HTTPException("Invalid request", HttpStatus.BAD_REQUEST_400);

    /**
     * The requested method is not allowed
     */
    public static final HTTPException ERROR_METHOD_NOT_ALLOWED = new HTTPException("Method not allowed", HttpStatus.METHOD_NOT_ALLOWED_405);

    /**
     * The requested object does not exist
     */
    public static final HTTPException ERROR_OBJECT_NOT_FOUND = new HTTPException("The referenced object does not exist", HttpStatus.BAD_REQUEST_400);

    /**
     * The requested route does not exist
     */
    public static final HTTPException ERROR_NOT_FOUND = new HTTPException("Not found", HttpStatus.NOT_FOUND_404);

    /**
     * The status that goes with this error
     */
    private HttpStatus status = HttpStatus.BAD_REQUEST_400;

    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, HttpStatus status) {
        this(message);
        setStatus(status);
    }

    /**
     * Getter for property 'status'
     *
     * @return Value for property 'status'
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'
     *
     * @param status Value to set for property 'status'
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
