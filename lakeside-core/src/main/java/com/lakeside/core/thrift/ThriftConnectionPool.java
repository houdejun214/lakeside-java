package com.lakeside.core.thrift;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.core.thrift.ThriftConnection.TServiceValidator;

/**
 * @author zhufb
 *
 * @param <R>
 */
public class ThriftConnectionPool<T extends TServiceClient & TServiceValidator> {
	
	private static final Logger log = LoggerFactory.getLogger("ThriftConnectionPool");
	private GenericObjectPool.Config poolConfig;
	private ThriftConnectionFactory<T> factory;
	private GenericObjectPool<ThriftConnection<T>> mPool;
	private Class<T> clientClass;
	private ThriftConfig cfg;
	
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
	public ThriftConnectionPool(Class<T> cls,ThriftConfig cfg,ThriftHostLoader loader) {
		this.clientClass = cls;
		this.cfg = cfg;
		poolConfig = new GenericObjectPool.Config();
		poolConfig.lifo = cfg.getBoolean("thrift.pool.nonfair.lifo", false);
		poolConfig.maxActive = cfg.getInt("thrift.pool.nonfair.maxActive", 10);
		poolConfig.maxIdle = cfg.getInt("thrift.pool.nonfair.maxIdle", 2);
		poolConfig.minIdle = cfg.getInt("thrift.pool.nonfair.minIdle", 1);
		poolConfig.maxWait = cfg.getInt("thrift.pool.nonfair.maxWait",20 * 1000);
		poolConfig.testOnBorrow = cfg.getBoolean("thrift.pool.nonfair.testOnBorrow", true);
		poolConfig.whenExhaustedAction = (byte)cfg.getInt("thrift.pool.nonfair.whenExhaustedAction", 1);
		poolConfig.timeBetweenEvictionRunsMillis = cfg.getInt("thrift.pool.nonfair.timeBetweenEvictionRunsMillis",3*60*1000);
		poolConfig.minEvictableIdleTimeMillis = cfg.getInt("thrift.pool.nonfair.minEvictableIdleTimeMillis",5*60*1000);
		poolConfig.numTestsPerEvictionRun = cfg.getInt("thrift.pool.nonfair.numTestsPerEvictionRun",3);
		this.factory = new ThriftConnectionFactory<T>(this, cfg,loader);
		this.init();
	}

	private void init() {
		mPool = new GenericObjectPool<ThriftConnection<T>>(factory, poolConfig);
		for (int i = 0; i < poolConfig.minIdle; i++) {
			this.add();
		}
	}
	
	private void add(){
		try {
			mPool.addObject();
		} catch (Exception e) {
			throw new ThriftException(
					"ThriftClientPool add thrift client failed", e);
		}
	}

	// * Thread safe
	public ThriftConnection<T> get() {
		try {
			ThriftConnection<T> client = mPool.borrowObject();
			return client;
		} catch (NoSuchElementException e) {
			log.warn("Get thrift client timeout,will try again later!");
			return this.get();
		}catch(Exception e){
			throw new ThriftException(
					"ThriftClientPool add thrift client failed", e);
		}
	}

	public void put(ThriftConnection<T> connection) {
		try {
			mPool.returnObject(connection);
		} catch (Exception e) {
			throw new ThriftException(
					"Non fair pool put thrift client back failed", e);
		}
	}

	public boolean remove(ThriftConnection<T> connection) {
		try {
			mPool.invalidateObject(connection);
			return true;
		} catch (Exception e) {
			throw new ThriftException(
					"Non fair pool remove thrift client failed", e);
		}
	}

	public void destroy() {
		try {
			mPool.close();
		} catch (Exception e) {
			throw new ThriftException("Non fair pool destroy failed", e);
		}

	}

	public void restart() {
		this.destroy();
		this.init();
	}

	public Collection<ThriftConnection<T>> values() {
		throw new ThriftException(
				"NonFairConnectionPool does not support this method");
	}

	protected Class<T> getClientClass() {
		return clientClass;
	}

	public int size() {
		return mPool.getNumIdle();
	}

	public ThriftConfig getCfg() {
		return cfg;
	}
}
