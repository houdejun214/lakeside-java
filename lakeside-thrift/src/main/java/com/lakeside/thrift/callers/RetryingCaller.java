package com.lakeside.thrift.callers;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryingCaller extends CallerDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(RetryingCaller.class.getName());
    private final ImmutableSet<Class<? extends Exception>> retryableExceptions;
    private final int retries;
    private final boolean debug;

    /**
     * Creates a new retrying caller. The retrying caller will attempt to call invoked methods on the
     * underlying caller at most {@code retries} times.  A retry will be performed only when one of
     * the {@code retryableExceptions} is caught.
     * @param decoratedCall       The caller to decorate with retries.
     * @param retries             The maximum number of retries to perform.
     * @param retryableExceptions The exceptions that can be retried.
     * @param debug               Whether to include debugging information when retries are being performed.
     */
    public RetryingCaller(Caller decoratedCall, int retries, ImmutableSet<Class<? extends Exception>> retryableExceptions,
                          boolean debug) {
        super(decoratedCall);
        this.retries = retries;
        this.retryableExceptions = retryableExceptions;
        this.debug = debug;
    }

    @Override
    public Object call(final Method method, final Object[] args,
                       final AsyncMethodCallback callback,
                       final Amount<Long, Time> connectTimeoutOverride) throws Throwable {

        AtomicInteger attempts = new AtomicInteger();
        Throwable exception = null;
        boolean continueLoop;
        try {
            do {
                try {
                    // If this is an async call, the looping will be handled within the capture.
                    return invoke(method, args, callback, null, connectTimeoutOverride);
                } catch (Throwable t) {
                    exception = t;
                    if (!isRetryable(t)) {
                        if (debug) {
                            LOG.debug(String.format(
                                    "Call failed with un-retryable exception of [%s]: %s, previous exceptions: %s",
                                    t.getClass().getName(), t.getMessage(), t.getLocalizedMessage()));
                        }
                        throw t;
                    }
                }
                continueLoop = attempts.incrementAndGet() <= retries;
            } while (continueLoop);
        }finally {
            // some exception found and retry fail
            if (exception!=null) {
                if(debug) {
                    LOG.debug(
                            String.format("Retried %d times, last error: %s",
                                    attempts.get(), exception));
                }
            }
        }
        throw exception;
    }

    private boolean isRetryable(Throwable throwable) {
        return isRetryable.getUnchecked(throwable.getClass());
    }

    private final LoadingCache<Class<? extends Throwable>, Boolean> isRetryable =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Throwable>, Boolean>() {
                @Override
                public Boolean load(Class<? extends Throwable> exceptionClass) {
                    return isRetryable(exceptionClass);
                }
            });

    private boolean isRetryable(final Class<? extends Throwable> exceptionClass) {
        if (retryableExceptions.contains(exceptionClass)) {
            return true;
        }
        return Iterables.any(retryableExceptions, new Predicate<Class<? extends Exception>>() {
            @Override
            public boolean apply(Class<? extends Exception> retryableExceptionClass) {
                return retryableExceptionClass.isAssignableFrom(exceptionClass);
            }
        });
    }

    private static final Joiner STACK_TRACE_JOINER = Joiner.on('\n');

    private static String combineStackTraces(List<Exception> exceptions) {
        if (exceptions.isEmpty()) {
            return "none";
        } else {
            return STACK_TRACE_JOINER.join(Iterables.transform(exceptions,
                    new Function<Throwable, String>() {
                        private int index = 1;

                        @Override
                        public String apply(Throwable exception) {
                            return String.format("[%d] %s",
                                    index++, Throwables.getStackTraceAsString(exception));
                        }
                    }));
        }
    }
}
