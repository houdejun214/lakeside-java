package com.lakeside.thrift;

/**
 * Created by dejun on 09/08/14.
 */
public class ConnectFailedException extends ThriftException {

    public ConnectFailedException(String message) {
        super(message);
    }

    public ConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        String s = ConnectFailedException.class.getSimpleName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
