package com.lakeside.thrift.callers;

import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.async.AsyncMethodCallback;

import java.lang.reflect.Method;

abstract class CallerDecorator implements Caller {
  private final Caller decoratedCaller;

  CallerDecorator(Caller decoratedCaller) {
    this.decoratedCaller = decoratedCaller;
  }

  /**
   * Convenience method for invoking the method and shunting the capture into the callback if
   * the call is asynchronous.
   *
   * @param method The method being invoked.
   * @param args The arguments to call {@code method} with.
   * @param callback The callback to use if the method is asynchronous.
   * @param capture The result capture to notify of the call result.
   * @param connectTimeoutOverride Optional override for the default connection timeout.
   * @return The return value from invoking the method.
   * @throws Throwable Exception, as prescribed by the method's contract.
   */
  protected final Object invoke(Method method, Object[] args,
      AsyncMethodCallback callback, final ResultCapture capture,
      Amount<Long, Time> connectTimeoutOverride) throws Throwable {

    // Swap the wrapped callback out for ours.
    if (callback != null) {
      callback = new WrappedMethodCallback(callback, capture);
    }

    try {
      Object result = decoratedCaller.call(method, args, callback, connectTimeoutOverride);
      if (callback == null && capture != null) capture.success();
      return result;
    } catch (Throwable t) {
      // We allow this one to go to both sync and async captures.
      if (callback != null) {
        callback.onError((Exception)t);
        return null;
      } else {
        if (capture != null) capture.fail(t);
        throw t;
      }
    }
  }
}
