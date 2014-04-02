package com.lakeside.thrift.pool;

import org.apache.thrift.TServiceClient;

import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.pool.ThriftConnection.TServiceValidator;

/**
 * BaseThriftConnectionPool
 *  
 * @author houdejun
 *
 * @param <T>
 */
public abstract class BaseThriftConnectionPool<T extends TServiceClient & TServiceValidator> {

	protected Class<T> clientClass;
	
	protected ThriftConfig cfg;
	
	/**
	 * get the class of thrift client 
	 * @return
	 */
	public Class<T> getClientClass() {
		return clientClass;
	}

	/**
	 * get thrift configuration
	 * @return
	 */
	public ThriftConfig getCfg() {
		return cfg;
	}
	
	/**
	 * return a thrift connection from the pool
	 * @param connection
	 */
	public abstract void put(ThriftConnection<T> connection);

	/**
	 * remove a connection from pool
	 * @param connection
	 * @return
	 */
	public abstract boolean remove(ThriftConnection<T> connection);
	
	/**
	 * Destroy whole pool
	 */
	public abstract void destroy();
	
	/**
	 * Reset the pool
	 */
	public abstract void restart() ;

	/**
	 * get the size of the pool
	 * @return
	 */
	public abstract int size() ;

}