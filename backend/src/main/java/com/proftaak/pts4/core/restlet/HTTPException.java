package com.proftaak.pts4.core.restlet;

import org.restlet.data.Status;

/**
 * @author Michon
 */
public class HTTPException extends Exception {
    /**
     * The user does not have permission to do what he wants to do
     */
    public static final HTTPException ERROR_FORBIDDEN = new HTTPException("You do not have permission to do that", Status.CLIENT_ERROR_FORBIDDEN);

    /**
     * The request is somehow invalid
     */
    public static final HTTPException ERROR_BAD_REQUEST = new HTTPException("Invalid request", Status.CLIENT_ERROR_BAD_REQUEST);

    /**
     * The requested method is not allowed
     */
    public static final HTTPException ERROR_METHOD_NOT_ALLOWED = new HTTPException("Method not allowed", Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

    /**
     * The requested object does not exist.
     */
    public static final HTTPException ERROR_OBJECT_NOT_FOUND = new HTTPException("The requested object does not exist", Status.CLIENT_ERROR_NOT_FOUND);

    /**
     * The status that goes with this error
     */
    private Status status = Status.SERVER_ERROR_INTERNAL;

    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, Status status) {
        this(message);
        setStatus(status);
    }

    /**
     * Getter for property 'status'
     *
     * @return Value for property 'status'
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'
     *
     * @param status Value to set for property 'status'
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
