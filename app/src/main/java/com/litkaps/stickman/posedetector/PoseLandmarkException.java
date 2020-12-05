package com.litkaps.stickman.posedetector;

/**
 * The class {@code PoseLandmarkException} indicates that an error related
 * to pose landmark was encountered.
 */
public class PoseLandmarkException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public PoseLandmarkException(String message) {
        super(message);
    }
}
