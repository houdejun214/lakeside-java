package com.lakeside.thrift;

import org.apache.thrift.TException;

/**
 * @author Adam Samet
 *
 * This is exception is thrown when accessing a thrift service resource times out.
 */
public class TTimeoutException extends TException {

  private static final long serialVersionUID = 1L;

  public TTimeoutException(String message) {
    super(message);
  }

  public TTimeoutException(Throwable cause) {
    super(cause);
  }

  public TTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
