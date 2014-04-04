package com.lakeside.thrift;

import java.net.SocketTimeoutException;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.thrift.pool.ThriftConnection;
import com.lakeside.thrift.pool.ThriftConnection.TServiceValidator;
import com.lakeside.thrift.pool.ThriftConnectionPool;


/**
 * 
 * ThriftTemplate 提供了一个template方法，负责对thrift连接的获取与归还。
 * 
 * @author zhufb
 *
 */
public class ThriftTemplate<T extends TServiceClient & TServiceValidator> {
	
	private static final Logger log = LoggerFactory.getLogger(ThriftTemplate.class);
	
	/**
	 * retry when network exception
	 */
	private static final int RETRY_ON_NET_EXCEPTION = 3;

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
		int i=0;
		R result = null;
		while(i++<RETRY_ON_NET_EXCEPTION){
			ThriftConnection<T> thriftConnection = null;
			try {
				 thriftConnection = pool.get();
				 return result = thriftAction.action(thriftConnection.getClient());
			} catch (TTransportException tte ) {
				tte.printStackTrace();
				log.warn("Get connection exception [{}] to server[{}], retry it",tte.getMessage(),thriftConnection.toString());
				// will lead to a retry.
				thriftConnection.close();
				thriftConnection.destroy();
				thriftConnection = null;
			} catch (SocketTimeoutException ste ) {
				log.warn("Get connection exception [{}] to server[{}], retry it",ste.getMessage(),thriftConnection.toString());
				// will lead to a retry.
				thriftConnection.close();
				thriftConnection.destroy();
				thriftConnection = null;
			} catch (Exception e) {
				throw new ThriftException(e);
			} finally {
				if(thriftConnection!=null){
					thriftConnection.close();
				}
			}
		}
		return result;
	}
	
	/**
	 * thrift action execute no result return
	 * 
	 * @param thriftAction
	 * @throws ThriftException
	 */
	public void execute(ThriftActionNoResult<T> thriftAction) throws ThriftException {
		int i=0;
		while(i++<RETRY_ON_NET_EXCEPTION){
			ThriftConnection<T> thriftConnection = null;
			try {
				thriftConnection = pool.get();
				thriftAction.action(thriftConnection.getClient());
				return;
			} catch (TTransportException tte ) {
				log.warn("Get connection exception [{}] to server[{}], retry it",tte.getMessage(),thriftConnection.toString());
				// will lead to a retry.
				thriftConnection.close();
				thriftConnection.destroy();
				thriftConnection = null;
			} catch (SocketTimeoutException ste ) {
				log.warn("Get connection exception [{}] to server[{}], retry it",ste.getMessage(),thriftConnection.toString());
				// will lead to a retry.
				thriftConnection.close();
				thriftConnection.destroy();
				thriftConnection = null;
			} catch (Exception e) {
				throw new ThriftException(e);
			} finally {
				if(thriftConnection!=null){
					thriftConnection.close();
				}
			}
		}
	}

	public ThriftConnectionPool<T> getPool() {
		return pool;
	}

	/**
	 * ThriftAction without result
	 * 
	 * @author zhufb
	 *
	 * @param <T>
	 */
	public interface ThriftActionNoResult<T> {
		public void action(T client) throws Exception;
	}
	
	/**
	 * ThriftAction
	 * 
	 * @author zhufb
	 *
	 */
	public interface ThriftAction<T,R> {
		public R action(T client) throws Exception;
	}

}

