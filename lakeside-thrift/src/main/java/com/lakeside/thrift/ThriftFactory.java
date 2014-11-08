package com.lakeside.thrift;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lakeside.thrift.loadbalancer.*;
import com.lakeside.thrift.pool.ThriftConnection;
import com.lakeside.thrift.pool.ThriftConnectionPool;
import com.lakeside.thrift.pool.ThriftPoolConfig;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

public class ThriftFactory<T extends TServiceClient & ThriftConnection.TServiceValidator> {

    private static final int DEFAULT_MAX_CONNECTIONS_PER_ENDPOINT = 10;

    private int maxConnectionsPerEndpoint = DEFAULT_MAX_CONNECTIONS_PER_ENDPOINT;
    private Class<T> serviceInterface;
    private boolean framedTransport;
    private String serviceName;
    private Config thriftConfig;

    public static <T extends TServiceClient & ThriftConnection.TServiceValidator> ThriftFactory<T> create(Class<T> serviceInterface) {
        return new ThriftFactory(serviceInterface);
    }

    /**
     * Creates a default factory that will use unframed blocking transport.
     *
     * @param serviceInterface The interface of the thrift service to make a client for.
     */
    private ThriftFactory(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
        this.framedTransport = false;
        Class<?> enclosingClass = getServiceEnclosingClass(serviceInterface);
        this.serviceName = enclosingClass.getSimpleName();
    }

    private void checkBaseState() {
        Preconditions.checkArgument(maxConnectionsPerEndpoint > 0,
                "Must allow at least 1 connection per endpoint; %s specified", maxConnectionsPerEndpoint);
    }

    public Thrift<T> build(DynamicHostSet hostSets) {
        checkBaseState();
        Preconditions.checkNotNull(hostSets);
        ThriftPoolConfig cfg = new ThriftPoolConfig();
        cfg.put("thrift.pool.transport.framed",String.valueOf(framedTransport));
        cfg.put("thrift.pool.maxTotalPerKey", String.valueOf(maxConnectionsPerEndpoint));
        cfg.put("thrift.pool.transport.socket_timeout", String.valueOf(thriftConfig.getSocketTimeout().as(Time.MILLISECONDS)));
        ThriftConnectionPool<T> pool = new ThriftConnectionPool(serviceInterface, cfg);
        ExecutorService executorService = createManagedThreadpool();
        Function<TTransport, T> clientFactory = createClientFactory(serviceInterface);
        LoadBalancer<HostAndPort> loadBalancer = createLoadBalancer(hostSets);
        return new Thrift<>(thriftConfig, executorService, pool, serviceName,
                serviceInterface, clientFactory,loadBalancer);
    }

    private ExecutorService createManagedThreadpool() {
        ThreadFactory threadFactory =
                new ThreadFactoryBuilder()
                        .setNameFormat("Thrift[" + serviceName + "][%d]")
                        .setDaemon(true)
                        .build();
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(), threadFactory);
    }

    public ThriftFactory<T> withThriftConfig(Config config) {
        this.thriftConfig = config;
        return this;
    }

    /**
     * Specifies the maximum number of connections that should be made to any single endpoint.
     *
     * @param maxConnectionsPerEndpoint Maximum number of connections per endpoint.
     * @return A reference to the factory.
     */
    public ThriftFactory<T> withMaxConnectionsPerEndpoint(int maxConnectionsPerEndpoint) {
        Preconditions.checkArgument(maxConnectionsPerEndpoint > 0);
        this.maxConnectionsPerEndpoint = maxConnectionsPerEndpoint;

        return this;
    }

    /**
     * Instructs the factory whether framed transport should be used.
     *
     * @param framedTransport Whether to use framed transport.
     * @return A reference to the factory.
     */
    public ThriftFactory<T> useFramedTransport(boolean framedTransport) {
        this.framedTransport = framedTransport;
        return this;
    }

    private LoadBalancer<HostAndPort> createLoadBalancer(DynamicSet<HostAndPort> backends) {
        LoadBalancingStrategy<HostAndPort> loadBalancingStrategy = createDefaultLoadBalancingStrategy();
        return LoadBalancerImpl.create(loadBalancingStrategy, backends);
    }

    private LoadBalancingStrategy<HostAndPort> createDefaultLoadBalancingStrategy() {
        RoundRobinStrategy<HostAndPort> strategy = new RoundRobinStrategy<>();
        return new MarkDeadStrategy<>(strategy);
    }

    private static <T> Function<TTransport, T> createClientFactory(Class<T> serviceInterface) {
        final Constructor<? extends T> implementationConstructor =
                findImplementationConstructor(serviceInterface);

        return new Function<TTransport, T>() {
            @Override
            public T apply(TTransport transport) {
                try {
                    return implementationConstructor.newInstance(new TBinaryProtocol(transport));
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static <T> Constructor<? extends T> findImplementationConstructor(
            final Class<T> serviceInterface) {
        Class<? extends T> implementationClass = serviceInterface;
        try {
            return implementationClass.getConstructor(TProtocol.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find a single argument TProtocol constructor "
                    + "in service client class: " + implementationClass);
        }
    }

    private static Class<?> getServiceEnclosingClass(final Class<?> serviceInterface) {
        Class<?> enclosingClass = serviceInterface.getEnclosingClass();
        if (enclosingClass == null) {
            enclosingClass = serviceInterface;
        }
        return enclosingClass;
    }
}
