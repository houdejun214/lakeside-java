package com.lakeside.thrift.callers;

import com.google.common.base.Throwables;
import com.lakeside.thrift.ResourceExhaustedException;
import com.lakeside.thrift.TTimeoutException;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.async.AsyncMethodCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

public class DeadlineCaller extends CallerDecorator {
    private final ExecutorService executorService;
    private final Amount<Long, Time> timeout;

    /**
     * Creates a new deadline caller.
     *
     * @param decoratedCaller The caller to decorate with a deadline.
     * @param executorService The executor service to use for performing calls.
     * @param timeout         The timeout by which the underlying call should complete in.
     */
    public DeadlineCaller(Caller decoratedCaller, ExecutorService executorService,
                          Amount<Long, Time> timeout) {
        super(decoratedCaller);
        this.executorService = executorService;
        this.timeout = timeout;
    }

    @Override
    public Object call(final Method method, final Object[] args,
                       final AsyncMethodCallback callback,
                       final Amount<Long, Time> connectTimeoutOverride) throws Exception {
        try {
            Future<Object> result = executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        return invoke(method, args, callback, null, connectTimeoutOverride);
                    } catch (Throwable t) {
                        Throwables.propagateIfInstanceOf(t, Exception.class);
                        throw new RuntimeException(t);
                    }
                }
            });

            try {
                return result.get(timeout.getValue(), timeout.getUnit().getTimeUnit());
            } catch (TimeoutException e) {
                result.cancel(true);
                throw new TTimeoutException(e);
            } catch (ExecutionException e) {
                throw (Exception) e.getCause();
            }
        } catch (RejectedExecutionException e) {
            throw new ResourceExhaustedException(e);
        } catch (InvocationTargetException e) {
            throw new Exception(e.getCause());
        }
    }
}
