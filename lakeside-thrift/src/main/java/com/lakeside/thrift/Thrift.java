package com.lakeside.thrift;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import com.lakeside.core.Closure;
import com.lakeside.thrift.callers.Caller;
import com.lakeside.thrift.callers.DeadlineCaller;
import com.lakeside.thrift.callers.RetryingCaller;
import com.lakeside.thrift.callers.ThriftCaller;
import com.lakeside.thrift.loadbalancer.LoadBalancer;
import com.lakeside.thrift.loadbalancer.SingleBackend;
import com.lakeside.thrift.pool.ThriftConnection;
import com.lakeside.thrift.pool.ThriftConnectionPool;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

public class Thrift<T extends TServiceClient & ThriftConnection.TServiceValidator> {

    private final Config defaultConfig;
    private final ExecutorService executorService;
    private final ThriftConnectionPool<T> connectionPool;
    private final String serviceName;
    private final Class<T> serviceInterface;
    private final Function<TTransport, T> clientFactory;
    private final LoadBalancer<HostAndPort> allBackends;

    private volatile Map<HostAndPort, T> clientBuffer = new WeakHashMap<>();

    public Thrift(Config config, ExecutorService executorService,
                  ThriftConnectionPool connectionPool, String serviceName,
                  Class<T> serviceInterface, Function<TTransport, T> clientFactory, LoadBalancer<HostAndPort> loadBalancer) {

        defaultConfig = Preconditions.checkNotNull(config);
        this.executorService = Preconditions.checkNotNull(executorService);
        this.connectionPool = Preconditions.checkNotNull(connectionPool);
        this.serviceName = Preconditions.checkNotNull(serviceName);
        this.serviceInterface = checkServiceInterface(serviceInterface);
        this.clientFactory = Preconditions.checkNotNull(clientFactory);
        this.allBackends = loadBalancer;
    }

    static <I> Class<I> checkServiceInterface(Class<I> serviceInterface) {
        Preconditions.checkNotNull(serviceInterface);
        return serviceInterface;
    }

    public void close() {
        connectionPool.destroy();
        executorService.shutdown();
    }

    /**
     * create a client to all endpoint, and choose random one for current caller
     *
     * @return
     */
    public T create() {
        Closure<ThriftConnection<T>> collectionCallback = null;
        return create(collectionCallback);
    }

    /**
     * create a client to all endpoint, and choose random one for current caller
     *
     * @param collectionCallback, collection callback, will apply this callback when get a collection from pool success.
     * @return
     */
    public T create(Closure<ThriftConnection<T>> collectionCallback) {
        return createClient(allBackends, defaultConfig, collectionCallback,defaultConfig.getRequestTimeout());
    }

    /**
     * create a client to a single endpoint
     *
     * @param endpoint
     * @return
     */
    public T create(HostAndPort endpoint) {
        T client = clientBuffer.get(endpoint);
        if (client == null) {
            synchronized (Thrift.class) {
                client = clientBuffer.get(endpoint);
                if (client == null) {
                    SingleBackend<HostAndPort> singleBackend = new SingleBackend<>(endpoint);
                    client = createClient(singleBackend, defaultConfig, null, defaultConfig.getRequestTimeout());
                    clientBuffer.put(endpoint, client);
                }
            }
        }
        return client;
    }

    /**
     * create a client to a single endpoint
     *
     * @param endpoint
     * @return
     */
    public T create(HostAndPort endpoint, Amount<Long, Time> requestTimeout) {
        T client = clientBuffer.get(endpoint);
        if (client == null) {
            synchronized (Thrift.class) {
                client = clientBuffer.get(endpoint);
                if (client == null) {
                    SingleBackend<HostAndPort> singleBackend = new SingleBackend<>(endpoint);
                    client = createClient(singleBackend, defaultConfig, null, requestTimeout);
                    clientBuffer.put(endpoint, client);
                }
            }
        }
        return client;
    }

    /**
     * create a thrift client for calling
     *
     * @param loadBalancer
     * @param config
     * @param collectionCallback, collection callback, will apply this callback when get a collection from pool success.
     * @return
     */
    private T createClient(LoadBalancer<HostAndPort> loadBalancer, Config config, Closure<ThriftConnection<T>> collectionCallback,Amount<Long, Time> requestTimeout) {
        // lease/call/[invalidate]/release
        boolean debug = config.isDebug();

        Caller decorated = new ThriftCaller<>(connectionPool, loadBalancer, clientFactory, collectionCallback);

        // [retry]
        if (config.getMaxRetries() > 0) {
            decorated = new RetryingCaller(decorated,  config.getMaxRetries(), config.getRetryableExceptions(), debug);
        }

        // [deadline]
        if (requestTimeout!=null && requestTimeout.getValue() > 0) {
            decorated = new DeadlineCaller(decorated, executorService, requestTimeout);
        }

        final Caller caller = decorated;

        final MethodInterceptor invocationHandler = new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                AsyncMethodCallback callback = null;
                return caller.call(method, args, callback, null);
            }
        };
        return (T) Enhancer.create(serviceInterface, invocationHandler);
    }
}
