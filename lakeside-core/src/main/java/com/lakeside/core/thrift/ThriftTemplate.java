package com.lakeside.core.thrift;

import org.apache.thrift.TServiceClient;

import com.lakeside.core.thrift.ThriftConnection.TServiceValidator;


/**
 * 
 * ThriftTemplate 提供了一个template方法，负责对thrift连接的获取与归还。
 * 
 * @author zhufb
 *
 */
public class ThriftTemplate<T extends TServiceClient & TServiceValidator> {

	private ThriftConnectionPool<T> pool;
	
	public ThriftTemplate(ThriftConnectionPool<T> pool) {
		this.pool = pool;
	}
	
	/**
	 * thrift action execute return R
	 * 
	 * @param thriftAction
	 * @return
	 * @throws ThriftException
	 */
	public <R> R execute(ThriftAction<T,R> thriftAction) throws ThriftException {
		ThriftConnection<T> thriftConnection = pool.get();
		try {
			return thriftAction.action(thriftConnection.getClient());
		} catch (Exception e) {
			throw new ThriftException(e);
		} finally {
			thriftConnection.close();
		}
	}
	
	/**
	 * thrift action execute no result return
	 * 
	 * @param thriftAction
	 * @throws ThriftException
	 */
	public void execute(ThriftActionNoResult<T> thriftAction) throws ThriftException {
		ThriftConnection<T> thriftConnection = pool.get();
		try {
			thriftAction.action(thriftConnection.getClient());
		} catch (Exception e) {
			throw new ThriftException(e);
		} finally {
			thriftConnection.close();
		}
	}

	public ThriftConnectionPool<T> getPool() {
		return pool;
	}

	/**
	 * HBaseAction for hbase 
	 * 
	 * @author zhufb
	 *
	 * @param <T>
	 */
	public interface ThriftActionNoResult<T> {
		public void action(T client) throws Exception;
	}
	
	/**
	 * HBaseAction for hbase 
	 * 
	 * @author zhufb
	 *
	 */
	public interface ThriftAction<T,R> {
		public R action(T client) throws Exception;
	}

}

