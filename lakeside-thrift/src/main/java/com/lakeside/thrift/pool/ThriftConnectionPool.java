package com.lakeside.thrift.pool;


import com.google.common.net.HostAndPort;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Grouped ThriftConnection 连接池
 *
 * @author houdejun
 * @param <T>
 */
public class ThriftConnectionPool<T extends TServiceClient & ThriftConnection.TServiceValidator> {

	private static final Logger log = LoggerFactory.getLogger(ThriftConnectionPool.class);
    private final Class<T> clientClass;
    private final ThriftPoolConfig cfg;
    private GenericKeyedObjectPoolConfig poolConfig;
	private ThriftConnectionFactory<T> factory;
	private GenericKeyedObjectPool<HostAndPort,ThriftConnection<T>> mPool;
	private boolean loop = false;

    /**
	 *  【数据库连接池 参数说明】
		#设置后进先出的池策略
		lifo=false
		#允许最大活动对象数
		maxActive=10
		#允许最大空闲对象数
		maxIdle=2
		#允许最大等待时间毫秒数
		maxWait=20000
		#被空闲对象回收器回收前在池中保持空闲状态的最小时间毫秒数
		minEvictableIdleTimeMillis=300000
		#允许最小空闲对象数
		minIdle=1
		#设定在进行后台对象清理时，每次检查对象数
		numTestsPerEvictionRun=3
		#指明是否在从池中取出对象前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个.
		testOnBorrow =true
		#指明是否在归还到池中前进行检验
		testOnReturn =false
		#指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.
		testWhileIdle=false
		#在空闲连接回收器线程运行期间休眠的时间毫秒数. 如果设置为非正数,则不运行空闲连接回收器线程
		timeBetweenEvictionRunsMillis=180000
		#当池中对象用完时，请求新的对象所要执行的动作
		whenExhaustedAction=1
	*/
	public ThriftConnectionPool(Class<T> cls, ThriftPoolConfig cfg) {
		this(cls, cfg, new ThriftConnectionFactory<>());
    }

    public ThriftConnectionPool(Class<T> cls, ThriftPoolConfig cfg,ThriftConnectionFactory connectionFactory) {
        this.clientClass = cls;
        this.cfg = cfg;
        poolConfig = new GenericKeyedObjectPoolConfig();
        poolConfig.setLifo(cfg.getBoolean("thrift.pool.lifo", false));
        poolConfig.setMaxTotal(cfg.getInt("thrift.pool.maxActive", 10));
        poolConfig.setMaxIdlePerKey(cfg.getInt("thrift.pool.maxIdlePerKey", 3));
        poolConfig.setMinIdlePerKey(cfg.getInt("thrift.pool.minIdlePerKey", 0));
        poolConfig.setMaxTotalPerKey(cfg.getInt("thrift.pool.maxTotalPerKey", 10));

        /**
         set the maximum amount of time (in milliseconds) the
         <code>borrowObject()</code> method should block before throwing an
         exception when the pool is exhausted and {@link #getBlockWhenExhausted} is true.
         When less than 0, the <code>borrowObject()</code> method may block indefinitely.
         */
        poolConfig.setMaxWaitMillis(cfg.getInt("thrift.pool.maxWait",20 * 1000));
        /**
         Sets whether to block when the <code>borrowObject()</code> method is
         invoked when the pool is exhausted (the maximum number of "active"
         objects has been reached).
         */
        poolConfig.setBlockWhenExhausted(true);
        /**为了提升性能 关闭testOnBorrow**/
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestWhileIdle(true);

        this.loop = cfg.getBoolean("thrift.pool.get.loop",false);

        // 开启一个线程执行扫描检测connection
        poolConfig.setTimeBetweenEvictionRunsMillis(cfg.getInt("thrift.pool.timeBetweenEvictionRunsMillis",3*60*1000));
        poolConfig.setMinEvictableIdleTimeMillis(cfg.getInt("thrift.pool.minEvictableIdleTimeMillis",5*60*1000));
        poolConfig.setNumTestsPerEvictionRun(cfg.getInt("thrift.pool.numTestsPerEvictionRun",3));
        this.factory = connectionFactory;
        this.factory.setPool(this);
        mPool = new GenericKeyedObjectPool<>(factory, poolConfig);
    }

	/**
	 * with default thrift configuration
	 * @param cls
	 */
	public ThriftConnectionPool(Class<T> cls) {
		this(cls,new ThriftPoolConfig());
	}

	/**
	 * borrow a thrift connection from the pool
	 * @return
	 */
	public ThriftConnection<T> get(HostAndPort endpoint) throws Exception {
	    ThriftConnection<T> connection = mPool.borrowObject(endpoint);
        return connection;
    }

    /**
     * borrow a thrift connection from the pool
     * @return
     */
    public ThriftConnection<T> get(HostAndPort endpoint, long borrowMaxWaitMillis) throws Exception {
        ThriftConnection<T> connection = mPool.borrowObject(endpoint,borrowMaxWaitMillis);
        return connection;
    }

	/**
	 * @param s
	 * @return
	 */
	private boolean wait(int s){
		try {
			Thread.sleep(s*1000);
		} catch (InterruptedException e) {

		}
		return true;
	}

	/**
	 * return a thrift connection from the pool
	 * @param connection
	 */
	public void put(HostAndPort endpoint,ThriftConnection<T> connection) {
		try {
			mPool.returnObject(endpoint,connection);
		} catch (Exception e) {
            connection.destroy();
            log.warn("put ThriftConnection [{}] failed and destroy it. {}",connection, e.getMessage());
		}
	}

	/**
	 * remove a connection from pool
	 * @param connection
	 * @return
	 */
	public boolean remove(HostAndPort endpoint,ThriftConnection<T> connection) {
		try {
			mPool.invalidateObject(endpoint,connection);
			return true;
		} catch (Exception e) {
            log.warn("Remove ThriftConnection [{}] failed. {}",connection, e.getMessage());
            return false;
		}
	}

	/**
	 * destory whole pool
	 */
	public void destroy() {
		try {
			mPool.close();
		} catch (Exception e) {
			throw new RuntimeException("Destroy pool failed", e);
		}
	}

	/**
	 * reset the pool
	 */
	public void restart() {
		this.destroy();
	}

	/**
	 * get the number of idle connection object
	 */
	public int size() {
		return mPool.getNumIdle();
	}

	public void put(ThriftConnection<T> connection) {
        HostAndPort groupKey = connection.getEndpoint();
		checkNotNull(groupKey);
		this.put(groupKey, connection);
	}

	public boolean remove(ThriftConnection<T> connection) {
        HostAndPort groupKey = connection.getEndpoint();
		checkNotNull(groupKey);
		return this.remove(groupKey, connection);
	}

    public ThriftPoolConfig getCfg() {
        return cfg;
    }

    public GenericKeyedObjectPool<HostAndPort, ThriftConnection<T>> getGenericPool() {
        return mPool;
    }
}
