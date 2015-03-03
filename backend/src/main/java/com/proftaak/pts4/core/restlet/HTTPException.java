package com.proftaak.pts4.core.restlet;

import org.restlet.data.Status;

/**
 * @author Michon
 */
public class HTTPException extends Exception {
    /**
     * The status that goes with this error.
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
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
