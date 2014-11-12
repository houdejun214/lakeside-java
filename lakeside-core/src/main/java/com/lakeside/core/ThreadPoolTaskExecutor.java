package com.lakeside.core;

import com.lakeside.core.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Executor service wrapper which support bounded feature.
 *
 * @author dejun
 */
public class ThreadPoolTaskExecutor extends AbstractExecutorService implements Executor{

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolTaskExecutor.class);

	private final Object poolSizeMonitor = new Object();

	private int corePoolSize = 1;

	private int maxPoolSize = Integer.MAX_VALUE;

	private int keepAliveSeconds = 60;

	private boolean allowCoreThreadTimeOut = false;

	private ThreadPoolExecutor executor;

    private int awaitTerminationSeconds = 0;

    private boolean waitForTasksToCompleteOnShutdown;

    private RejectedExecutionHandler rejectedExecutionHandler;

    /**
     * Set the ThreadPoolExecutor's core pool size.
	 * Default is 1.
	 * <p><b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setCorePoolSize(int corePoolSize) {
		synchronized (this.poolSizeMonitor) {
			this.corePoolSize = corePoolSize;
			if (this.executor != null) {
				this.executor.setCorePoolSize(corePoolSize);
			}
		}
	}

	/**
	 * Return the ThreadPoolExecutor's core pool size.
	 */
	public int getCorePoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.corePoolSize;
		}
	}

	/**
	 * Set the ThreadPoolExecutor's maximum pool size.
	 * Default is {@code Integer.MAX_VALUE}.
	 * <p><b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		synchronized (this.poolSizeMonitor) {
			this.maxPoolSize = maxPoolSize;
			if (this.executor != null) {
				this.executor.setMaximumPoolSize(maxPoolSize);
			}
		}
	}

	/**
	 * Return the ThreadPoolExecutor's maximum pool size.
	 */
	public int getMaxPoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.maxPoolSize;
		}
	}

	/**
	 * Set the ThreadPoolExecutor's keep-alive seconds.
	 * Default is 60.
	 * <p><b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		synchronized (this.poolSizeMonitor) {
			this.keepAliveSeconds = keepAliveSeconds;
			if (this.executor != null) {
				this.executor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
			}
		}
	}

	/**
	 * Return the ThreadPoolExecutor's keep-alive seconds.
	 */
	public int getKeepAliveSeconds() {
		synchronized (this.poolSizeMonitor) {
			return this.keepAliveSeconds;
		}
	}

	/**
	 * Specify whether to allow core threads to time out. This enables dynamic
	 * growing and shrinking even in combination with a non-zero queue (since
	 * the max pool size will only grow once the queue is full).
	 * <p>Default is "false". Note that this feature is only available on Java 6
	 * or above. On Java 5, consider switching to the backport-concurrent
	 * version of ThreadPoolTaskExecutor which also supports this feature.
	 * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)
	 */
	public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

    /**
     * Set whether to wait for scheduled tasks to complete on shutdown,
     * not interrupting running tasks and executing all tasks in the queue.
     * <p>Default is "false", shutting down immediately through interrupting
     * ongoing tasks and clearing the queue. Switch this flag to "true" if you
     * prefer fully completed tasks at the expense of a longer shutdown phase.
     * <p>Note that Spring's container shutdown continues while ongoing tasks
     * are being completed. If you want this executor to block and wait for the
     * termination of tasks before the rest of the container continues to shut
     * down - e.g. in order to keep up other resources that your tasks may need -,
     * set the {@link #setAwaitTerminationSeconds "awaitTerminationSeconds"}
     * property instead of or in addition to this property.
     * @see java.util.concurrent.ExecutorService#shutdown()
     * @see java.util.concurrent.ExecutorService#shutdownNow()
     */
    public void setWaitForTasksToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
    }

    /**
     * Set the maximum number of seconds that this executor is supposed to block
     * on shutdown in order to wait for remaining tasks to complete their execution
     * before the rest of the container continues to shut down. This is particularly
     * useful if your remaining tasks are likely to need access to other resources
     * that are also managed by the container.
     * <p>By default, this executor won't wait for the termination of tasks at all.
     * It will either shut down immediately, interrupting ongoing tasks and clearing
     * the remaining task queue - or, if the
     * {@link #setWaitForTasksToCompleteOnShutdown "waitForTasksToCompleteOnShutdown"}
     * flag has been set to {@code true}, it will continue to fully execute all
     * ongoing tasks as well as all remaining tasks in the queue, in parallel to
     * the rest of the container shutting down.
     * <p>In either case, if you specify an await-termination period using this property,
     * this executor will wait for the given time (max) for the termination of tasks.
     * As a rule of thumb, specify a significantly higher timeout here if you set
     * "waitForTasksToCompleteOnShutdown" to {@code true} at the same time,
     * since all remaining tasks in the queue will still get executed - in contrast
     * to the default shutdown behavior where it's just about waiting for currently
     * executing tasks that aren't reacting to thread interruption.
     * @see java.util.concurrent.ExecutorService#shutdown()
     * @see java.util.concurrent.ExecutorService#awaitTermination
     */
    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        synchronized (this.poolSizeMonitor) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
            if (this.executor != null) {
                this.executor.setRejectedExecutionHandler(rejectedExecutionHandler);
            }
        }
    }

    public void initialize(int queueCapacity) {
        this.initialize(queueCapacity, false);
    }

    /**
     *
     * workerQueue.offer
     * @param queueCapacity,  Capacity of the executor service, reject the task when the queue if full.
     * @param forceWait if ture, the execute operation will be blocked util the queue have space.
     */
	public void initialize(int queueCapacity, boolean forceWait) {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        if(rejectedExecutionHandler==null) {
            rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
        }
		BlockingQueue<Runnable> queue = createQueue(queueCapacity,forceWait);
		ThreadPoolExecutor executor  = new ThreadPoolExecutor(
				this.corePoolSize,
                this.maxPoolSize,
                this.keepAliveSeconds,
                TimeUnit.SECONDS,
				queue,
                threadFactory,
                rejectedExecutionHandler);
		if (this.allowCoreThreadTimeOut) {
			executor.allowCoreThreadTimeOut(true);
		}
		this.executor = executor;
	}

	/**
	 * Create the BlockingQueue to use for the ThreadPoolExecutor.
	 * <p>A LinkedBlockingQueue instance will be created for a positive
	 * capacity value; a SynchronousQueue else.
	 * @param queueCapacity the specified queue capacity
	 * @param forceWait
     * @return the BlockingQueue instance
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	protected BlockingQueue<Runnable> createQueue(int queueCapacity, boolean forceWait) {
		if (queueCapacity > 0 && forceWait) {
			return new WaitLinkedBlockingWorkerQueue<>(queueCapacity);
		}
        else if (queueCapacity > 0) {
            return new LinkedBlockingQueue<>(queueCapacity);
        }else {
			return new SynchronousQueue<>();
		}
	}

	/**
	 * Return the underlying ThreadPoolExecutor for native access.
	 * @return the underlying ThreadPoolExecutor (never {@code null})
	 * @throws IllegalStateException if the ThreadPoolTaskExecutor hasn't been initialized yet
	 */
	public ThreadPoolExecutor getExecutor() throws IllegalStateException {
		Assert.state(this.executor != null, "ThreadPoolTaskExecutor not initialized");
		return this.executor;
	}

	/**
	 * Return the current pool size.
	 * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()
	 */
	public int getPoolSize() {
		return getExecutor().getPoolSize();
	}

	/**
	 * Return the number of currently active threads.
	 * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount() {
		return getExecutor().getActiveCount();
	}


	public void execute(Runnable task) {
		Executor executor = getExecutor();
        executor.execute(task);
	}

	public void execute(Runnable task, long startTimeout) {
		execute(task);
	}

	public Future<?> submit(Runnable task) {
		ExecutorService executor = getExecutor();
        return executor.submit(task);
	}

	public <T> Future<T> submit(Callable<T> task) {
		ExecutorService executor = getExecutor();
        return executor.submit(task);
	}

    /**
     * Calls {@code shutdown} when the BeanFactory destroys
     * the task executor instance.
     * @see #shutdown()
     */
    public void destroy() {
        shutdown();
    }

    /**
     * Perform a shutdown on the underlying ExecutorService.
     * @see java.util.concurrent.ExecutorService#shutdown()
     * @see java.util.concurrent.ExecutorService#shutdownNow()
     * @see #awaitTerminationIfNecessary()
     */
    public void shutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("Shutting down ExecutorService");
        }
        if (this.waitForTasksToCompleteOnShutdown) {
            this.executor.shutdown();
        }
        else {
            this.executor.shutdownNow();
        }
        awaitTerminationIfNecessary();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.executor.awaitTermination(timeout,unit);
    }

    /**
     * Wait for the executor to terminate, according to the value of the
     * {@link #setAwaitTerminationSeconds "awaitTerminationSeconds"} property.
     */
    private void awaitTerminationIfNecessary() {
        if (this.awaitTerminationSeconds > 0) {
            try {
                if (!this.executor.awaitTermination(this.awaitTerminationSeconds, TimeUnit.SECONDS)) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Timed out while waiting for executor  to terminate");
                    }
                }
            }
            catch (InterruptedException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Interrupted while waiting for executor to terminate");
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class WaitLinkedBlockingWorkerQueue<E> extends LinkedBlockingDeque<E> {

        private WaitLinkedBlockingWorkerQueue(int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(E e) {
            try {
                this.put(e);
            } catch (InterruptedException e1) {
                throw new RejectedExecutionException("Fail to put worker "+e);
            }
            return true;
        }
    }
}