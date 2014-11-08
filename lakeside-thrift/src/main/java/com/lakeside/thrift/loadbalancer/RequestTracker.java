package com.lakeside.thrift.loadbalancer;

public interface RequestTracker<T> {

  /**
   * Informs the tracker of a completed request.
   *
   * @param key Key to identify the owner of the request.
   * @param result Result of the request.
   * @param requestTimeNanos Time duration spent waiting for the request to complete.
   */
  void requestResult(T key, RequestResult result, long requestTimeNanos);

  enum RequestResult {
    FAILED,
    TIMEOUT,
    SUCCESS,
    DEAD,
  }
}