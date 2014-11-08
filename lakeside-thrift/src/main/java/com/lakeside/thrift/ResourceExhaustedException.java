package com.lakeside.thrift;

/**
 */
public class ResourceExhaustedException extends RuntimeException {

    public ResourceExhaustedException(String message) {
        super(message);
    }

    public ResourceExhaustedException(Throwable cause) {
        super(cause);
    }

    public ResourceExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}