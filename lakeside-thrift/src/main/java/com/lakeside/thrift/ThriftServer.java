package com.lakeside.thrift;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lakeside.core.ExceptionalFunction;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ThriftServer {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftServer.class.getName());

    public static final Supplier<TProtocolFactory> BINARY_PROTOCOL =
            new Supplier<TProtocolFactory>() {
                @Override
                public TProtocolFactory get() {
                    return new TBinaryProtocol.Factory(false, true);
                }
            };
    public static final Supplier<TProtocolFactory> COMPACT_PROTOCOL =
            new Supplier<TProtocolFactory>() {
                @Override
                public TProtocolFactory get() {
                    return new TCompactProtocol.Factory();
                }
            };

    /**
     * This field actually provides a THsHaServer (Nonblocking server which had a thread pool)
     */
    public static final ExceptionalFunction<ServerSetup, TServer, TTransportException>
            NONBLOCKING_SERVER = new ExceptionalFunction<ServerSetup, TServer, TTransportException>() {
        @Override
        public TServer apply(ServerSetup setup) throws TTransportException {

            TNonblockingServerSocket socket = setup.getSocketTimeout() == null
                    ? new TNonblockingServerSocket(new InetSocketAddress("0.0.0.0", setup.getPort()))
                    : new TNonblockingServerSocket(new InetSocketAddress("0.0.0.0", setup.getPort()),
                    setup.getSocketTimeout().as(Time.MILLISECONDS));

            setup.setSocket(getServerSocketFor(socket));
            THsHaServer.Args options = new THsHaServer.Args(socket);
            options.processor(setup.getProcessor());
            if (setup.getNumThreads() > 0) {
                options.workerThreads(setup.getNumThreads());
            }
            // default queue size to num threads:  max response time becomes double avg service time
            final BlockingQueue<Runnable> queue =
                    new ArrayBlockingQueue<Runnable>(setup.getQueueSize() > 0 ? setup.getQueueSize()
                            : options.getWorkerThreads());
            final ThreadPoolExecutor invoker = new ThreadPoolExecutor(options.getWorkerThreads(),
                    options.getWorkerThreads(), options.getStopTimeoutVal(), options.getStopTimeoutUnit(), queue);

            final String serverName = (setup.getName() != null ? setup.getName() : "no-name");
            return new THsHaServer(options);
        }
    };

    /**
     * Thrift doesn't provide access to socket it creates,
     * this is the only way to know what ephemeral port we bound to.
     * TODO:  Patch thrift to provide access so we don't have to do this.
     */
    @VisibleForTesting
    static ServerSocket getServerSocketFor(TNonblockingServerSocket thriftSocket)
            throws TTransportException {
        try {
            Field field = TNonblockingServerSocket.class.getDeclaredField("serverSocket_");
            field.setAccessible(true);
            return (ServerSocket) field.get(thriftSocket);
        } catch (NoSuchFieldException e) {
            throw new TTransportException("Couldn't get listening port", e);
        } catch (SecurityException e) {
            throw new TTransportException("Couldn't get listening port", e);
        } catch (IllegalAccessException e) {
            throw new TTransportException("Couldn't get listening port", e);
        }
    }

    private final String name;
    private final String version;

    private ServerSetup serverSetup = null;

    private TServer server = null;

    // The thread that is responsible for invoking the blocking {@link TServer.serve()} call.
    private Thread listeningThread;

    // Current health status of the server.
    private Status status = Status.STARTING;

    // Time at which the server went live.  Should only be used for relative (duration) tracking.
    private long serverStartNanos = -1;
    private final Supplier<TProtocolFactory> protoFactorySupplier;
    private final ExceptionalFunction<ServerSetup, TServer, TTransportException> serverSupplier;

    /**
     * Creates a new default thrift server, which uses a TThreadPoolServer and
     *
     * @param name    Name for the server.
     * @param version Version identifier.
     */
    public ThriftServer(String name, String version) {
        this(name, version, COMPACT_PROTOCOL, NONBLOCKING_SERVER);
    }

    /**
     * Creates a new thrift server with the provided configuration.
     *
     * @param name                 Name for the server.
     * @param version              Version identifier.
     * @param protoFactorySupplier Supplier to build the protocol factory to use.
     * @param serverSupplier       Function to build a TServer object based on the server setup.
     */
    public ThriftServer(String name, String version, Supplier<TProtocolFactory> protoFactorySupplier,
                        ExceptionalFunction<ServerSetup, TServer, TTransportException> serverSupplier) {
        this.name = Preconditions.checkNotNull(name);
        this.version = Preconditions.checkNotNull(version);
        this.protoFactorySupplier = Preconditions.checkNotNull(protoFactorySupplier);
        this.serverSupplier = Preconditions.checkNotNull(serverSupplier);
    }

    /**
     * Starts the server.
     * This may be called at any point except when the server is already alive.  That is, it's
     * allowable to start, stop, and re-start the server.
     *
     * @param port      The port to listen on.
     * @param processor The processor to handle requests.
     */
    public void start(int port, TProcessor processor) {
        start(new ServerSetup(name, port, processor, protoFactorySupplier.get()));
    }

    /**
     * Starts the server.
     * This may be called at any point except when the server is already alive.  That is, it's
     * allowable to start, stop, and re-start the server.
     *
     * @param setup _options for server
     */
    public void start(ServerSetup setup) {
        Preconditions.checkNotNull(setup.getProcessor());
        Preconditions.checkState(status != Status.ALIVE, "Server must only be started once.");
        setStatus(Status.ALIVE);
        try {
            doStart(setup);
        } catch (TTransportException e) {
            LOG.info("Failed to open thrift socket.", e);
            setStatus(Status.DEAD);
        }
    }

    @VisibleForTesting
    protected void doStart(ServerSetup setup) throws TTransportException {
        serverSetup = setup;
        server = serverSupplier.apply(setup);
        serverStartNanos = System.nanoTime();
        LOG.info("Starting thrift server on port " + getListeningPort());

        listeningThread = new ThreadFactoryBuilder().setDaemon(false).build().newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.serve();
                } catch (Throwable t) {
                    LOG.warn(
                            "Uncaught exception while attempting to handle service requests: " + t, t);
                    setStatus(Status.DEAD);
                }
            }
        });

        listeningThread.start();
    }

    public ThriftServer awaitForAlive() {
        if (server != null) {
            while (!server.isServing()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    public int getListeningPort() {
        Preconditions.checkState(serverSetup != null);
        Preconditions.checkState(status == Status.ALIVE);
        Preconditions.checkState(serverSetup.getSocket() != null);
        return serverSetup.getSocket().getLocalPort();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Changes the status of the server.
     *
     * @param status New status.
     */
    protected void setStatus(Status status) {
        LOG.info("Moving from status " + this.status + " to " + status);
        this.status = status;
    }

    public long uptime() {
        return TimeUnit.SECONDS.convert(System.nanoTime() - serverStartNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Notification to the server that a shutdown request has been made, and the server is no longer
     * processing requests.  The implementer may veto the shutdown by throwing an exception.  A veto
     * would suggest a failure to terminate backend connections in a timely manner.
     *
     * @throws Exception If the shutdown request could not be honored.
     */
    protected void tryShutdown() throws Exception {
        // Default no-op.
    }

    /**
     * Attempts to shut down the server.
     * The server may be shut down at any time, though the request will be ignored if the server is
     * already stopped.
     */
    public void shutdown() {
        if (status == Status.STOPPED) {
            LOG.info("Server already stopped, shutdown request ignored.");
            return;
        }

        LOG.info("Received shutdown request, stopping server.");
        setStatus(Status.STOPPING);

        if (server != null) server.stop();
        server = null;

        // TODO(William Farner): Figure out what happens to queued / in-process requests when the server is
        // stopped.  Might want to allow a sleep period for the active requests to be completed.

        try {
            tryShutdown();
        } catch (Exception e) {
            LOG.warn("Service handler vetoed shutdown request.", e);
            setStatus(Status.WARNING);
            return;
        }

        setStatus(Status.STOPPED);
    }

    /**
     * Attempts to shut down this server, and waits for the shutdown operation to complete.
     *
     * @param timeout Maximum amount of time to wait for shutdown before giving up.  a timeout of
     *                zero means wait forever.
     * @throws InterruptedException If interrupted while waiting for shutdown.
     */
    public void awaitShutdown(Amount<Long, Time> timeout) throws InterruptedException {
        Preconditions.checkNotNull(timeout);
        shutdown();

        if (listeningThread != null) {
            listeningThread.join(timeout.as(Time.MILLISECONDS));
        }
    }

    /**
     * Represents the server configuration variables needed to construct a TServer.
     */
    public static final class ServerSetup {
        private final String name;
        private final int port;
        private final TProcessor processor;
        private final TProtocolFactory protoFactory;
        private final int numThreads;
        private final int queueSize;

        private ServerSocket socket = null;

        /**
         * Timeout for client sockets from accept
         */
        private final Amount<Integer, Time> socketTimeout;

        public ServerSetup(String name, int port, TProcessor processor, TProtocolFactory protoFactory) {
            this(name, port, processor, protoFactory, -1, -1, Amount.of(0, Time.MILLISECONDS));
        }

        public ServerSetup(String name, int port, TProcessor processor, TProtocolFactory protoFactory,
                           int numThreads, int queueSize, Amount<Integer, Time> socketTimeout) {
            Preconditions.checkArgument(port >= 0 && port < 0xFFFF, "Invalid port: " + port);
            Preconditions.checkArgument(numThreads != 0);
            Preconditions.checkArgument(queueSize != 0);
            if (socketTimeout != null) Preconditions.checkArgument(socketTimeout.getValue() >= 0);
            this.name = name;
            this.port = port;
            this.processor = processor;
            this.protoFactory = protoFactory;
            this.numThreads = numThreads;
            this.queueSize = queueSize;
            this.socketTimeout = socketTimeout;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }

        public int getNumThreads() {
            return numThreads;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public Amount<Integer, Time> getSocketTimeout() {
            return socketTimeout;
        }

        public TProcessor getProcessor() {
            return processor;
        }

        public TProtocolFactory getProtoFactory() {
            return protoFactory;
        }

        public ServerSocket getSocket() {
            return socket;
        }

        public void setSocket(ServerSocket socket) {
            this.socket = socket;
        }
    }
}
