package com.lakeside.thrift.callers;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.lakeside.core.Closure;
import com.lakeside.thrift.ConnectFailedException;
import com.lakeside.thrift.ResourceExhaustedException;
import com.lakeside.thrift.TTimeoutException;
import com.lakeside.thrift.loadbalancer.LoadBalancer;
import com.lakeside.thrift.loadbalancer.RequestTracker;
import com.lakeside.thrift.pool.ThriftConnection;
import com.lakeside.thrift.pool.ThriftConnectionPool;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

/**
 * A caller that issues calls to a target that is assumed to be a client to a thrift service.
 */
public class ThriftCaller<T extends TServiceClient & ThriftConnection.TServiceValidator> implements Caller {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftCaller.class.getName());

    private final LoadBalancer<HostAndPort> loadBalancer;
    private final ThriftConnectionPool<T> connectionPool;
    private final Function<TTransport, T> clientFactory;
    private final Closure<ThriftConnection<T>> collectionCallback;

    public ThriftCaller(ThriftConnectionPool<T> connectionPool,
                        LoadBalancer<HostAndPort> loadBalancer, Function<TTransport, T> clientFactory, Closure<ThriftConnection<T>> collectionCallback) {
        this.connectionPool = connectionPool;
        this.loadBalancer = loadBalancer;
        this.clientFactory = clientFactory;
        this.collectionCallback = collectionCallback;
    }

    @Override
    public Object call(Method method, Object[] args, AsyncMethodCallback callback,
                       Amount<Long, Time> connectTimeoutOverride) throws Throwable {

        final ThriftConnection<T> connection = getConnection(connectTimeoutOverride);
        final long startNanos = System.nanoTime();
        ResultCapture capture = new ResultCapture() {
            @Override
            public void success() {
                if (collectionCallback != null) {
                    try {
                        collectionCallback.execute(connection);
                        loadBalancer.requestResult(connection.getEndpoint(),
                                RequestTracker.RequestResult.SUCCESS, System.nanoTime() - startNanos);
                    } catch (Exception e) {
                        //ignore the errors.
                        LOG.warn(e.getMessage(),e);
                    }
                }
                connectionPool.put(connection);
            }

            @Override
            public boolean fail(Throwable t) {
                try {
                    RequestTracker.RequestResult result = RequestTracker.RequestResult.DEAD;
                    if (t instanceof TTimeoutException) {
                        result = RequestTracker.RequestResult.TIMEOUT;
                    }else if(t instanceof TTransportException) {
                        result = RequestTracker.RequestResult.DEAD;
                    }
                    loadBalancer.requestResult(connection.getEndpoint(),
                            result, System.nanoTime() - startNanos);

                } finally {
                    connectionPool.remove(connection);
                }
                return true;
            }
        };

        return invokeMethod(clientFactory.apply(connection.get()), method, args, callback, capture);
    }

    private static Object invokeMethod(Object target, Method method, Object[] args,
                                       AsyncMethodCallback callback, final ResultCapture capture) throws Throwable {

        // Swap the wrapped callback out for ours.
        if (callback != null) {
            callback = new WrappedMethodCallback(callback, capture);

            List<Object> argsList = Lists.newArrayList(args);
            argsList.add(callback);
            args = argsList.toArray();
        }

        try {
            Object result = method.invoke(target, args);
            if (callback == null) capture.success();
            return result;
        } catch (InvocationTargetException t) {
            // We allow this one to go to both sync and async captures.
            if (callback != null) {
                callback.onError((Exception) t.getCause());
                return null;
            } else {
                capture.fail(t.getCause());
                throw t.getCause();
            }
        }
    }

    private ThriftConnection<T> getConnection(
            Amount<Long, Time> connectTimeoutOverride)
            throws Exception {
        HostAndPort endpoint = null;
        try {
            endpoint = this.loadBalancer.nextBackend();
            ThriftConnection<T> connection;
            if (connectTimeoutOverride != null) {
                connection = connectionPool.get(endpoint, connectTimeoutOverride.as(Time.MILLISECONDS));
            } else {
                connection = connectionPool.get(endpoint);
            }
            if (connection == null) {
                throw new ResourceExhaustedException("no connection was available");
            }
            return connection;
        } catch (NoSuchElementException e) {
            throw new ResourceExhaustedException(e);
        } catch (TimeoutException e) {
            throw new TTimeoutException(e);
        } catch (ConnectFailedException e) {
            loadBalancer.requestResult(endpoint, RequestTracker.RequestResult.DEAD, 0);
            throw e;
        }
    }
}
