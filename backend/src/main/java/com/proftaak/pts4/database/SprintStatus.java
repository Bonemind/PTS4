package com.proftaak.pts4.database;

/**
 * @author Michon
 */
public enum SprintStatus {
    /**
     * Story/task is defined.
     */
    DEFINED,

    /**
     * Story/task has been started.
     */
    IN_PROGRESS,

    /**
     * Story/task has been completed, is now waiting for verification.
     */
    DONE,

    /**
     * Story/task has been accepted.
     */
    ACCEPTED
}
