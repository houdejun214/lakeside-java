package com.lakeside.thrift.pool;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.exception.ThriftException;
import com.lakeside.thrift.host.ThriftHost;
import com.lakeside.thrift.pool.ThriftConnection.TServiceValidator;

/**
 * 
 * A Thrift Client represent for a thrift connection (thrift socket) to a thrift server.It must be bound with a TServiceClient.
 * 
 * @author zhufb
 *
 */
public class ThriftConnection<T extends TServiceClient & TServiceValidator> {

	private boolean mClosed = false;
	private final T client;
	private final ThriftConnectionPool<T> pool;
	private final ThriftHost regionHost;

	public ThriftConnection(ThriftConnectionPool<T> pool, ThriftHost rh) {
		this.pool = pool;
		this.regionHost = rh;
		this.client = createThriftClient();
	}
	
	/**
	 * this constructor only available in test
	 * @param pool
	 * @param rh
	 * @param client
	 */
	ThriftConnection(ThriftConnectionPool<T> pool, ThriftHost rh,T client) {
		this.pool = pool;
		this.regionHost = rh;
		this.client = client;
	}

	public T getClient() {
		return client;
	}

	/**
	 * close this connections, this is not real close the connection, only return the connection to pool
	 */
	public void close() {
		pool.put(this);
	}

	/**
	 * destroy this connections
	 */
	public void destroy() {
		client.getInputProtocol().getTransport().close();
		mClosed = true;
		pool.remove(this);
	}

	/**
	 * validate the connection is available
	 * @return the closed
	 * @throws IOException 
	 */
	public boolean validate()  {
		if(mClosed){
			return false;
		}
		try{
			return client.validate();
		} catch (Exception e){
			return false;
		}
	}
	
	/**
	 * create thrift client
	 * @return
	 */
	private T createThriftClient() {
		String host = regionHost.getIp();
		int port = regionHost.getPort();
		int timeout = 5*60*1000;
		TSocket socket = new TSocket(host, port,timeout);
	    TProtocol protocol = new TBinaryProtocol(socket, true, true);
		try {
			ThriftConfig cfg = pool.getCfg();
			Class<T>  type =  (Class<T>)pool.getClientClass();
			T client = null;
			Constructor<?> constructor = getConstructor(type,TProtocol.class,ThriftConfig.class);
			if(constructor!=null){
				client = (T) constructor.newInstance(protocol,cfg);
			}else{
				constructor = getConstructor(type,TProtocol.class);
				client = (T) constructor.newInstance(protocol);
			}
			socket.open();
			return client;
		} catch (TTransportException e) {
			throw new ThriftException("Failed to open the connection to "+regionHost, e);
		} catch (Exception e) {
			throw new ThriftException("create thirft client failed", e);
		}
	}
	
	/**
	 * get a constructor of a type
	 * @param type
	 * @param parameterTypes
	 * @return
	 */
	private Constructor<?> getConstructor(Class<?> type,Class<?>... parameterTypes){
		try {
			Constructor<?> constructor = type.getConstructor(parameterTypes);
			return constructor;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		if(regionHost!=null){
			return regionHost.getIp()+":"+this.regionHost.getPort();
		}
		return super.toString();
	}
	
	public static interface TServiceValidator{
		public boolean validate();
	}
}
