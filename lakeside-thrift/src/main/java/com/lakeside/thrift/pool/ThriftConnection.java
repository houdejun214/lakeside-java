package com.lakeside.thrift.pool;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.ThriftException;
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
	private String groupKey = null;
	private boolean mClosed = false;
	private final T client;
	private final BaseThriftConnectionPool<T> pool;
	private final ThriftHost regionHost;
	private boolean compact = true;
	private boolean framed = true;
	private int timeout = 5*60*1000;

	public ThriftConnection(BaseThriftConnectionPool<T> pool, ThriftHost rh) {
		this.pool = pool;
		this.regionHost = rh;
		this.compact = pool.getCfg().getBoolean("thrift.pool.protocol.compact",true);
		this.framed = pool.getCfg().getBoolean("thrift.pool.transport.framed",true);
		this.timeout = pool.getCfg().getInt("thrift.pool.transport.timeout",5*60*1000);
		this.client = createThriftClient();
	}
	
	/**
	 * this constructor only available in test
	 * @param pool
	 * @param rh
	 * @param client
	 */
	ThriftConnection(BaseThriftConnectionPool<T> pool, ThriftHost rh,T client) {
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
		TProtocol protocol = client.getInputProtocol();
		if(protocol!=null){
			TTransport transport = protocol.getTransport();
			if(transport!=null)transport.close();
		}
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
	    TProtocol protocol = newTProtocol();
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
			protocol.getTransport().open();
			return client;
		} catch (TTransportException e) {
			throw new ThriftException("Failed to open the connection to "+regionHost, e);
		} catch (Exception e) {
			throw new ThriftException("create thirft client failed", e);
		}
	}
	
	private TProtocol newTProtocol(){
		String host = regionHost.getIp();
		int port = regionHost.getPort();
		TTransport transport = new TSocket(host, port,timeout);
		if(framed){
			transport = new TFramedTransport(transport);
		}
		return compact?new TCompactProtocol(transport):new TBinaryProtocol(transport);
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
	
	protected String getGroupKey() {
		return groupKey;
	}

	/**
	 * set the groupKey when work with ThriftGroupConnectionPool
	 * @param groupKey
	 */
	protected void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}

	/**
	 * get the groupKey when work with ThriftGroupConnectionPool
	 * @author houdejun
	 *
	 */
	public static interface TServiceValidator{
		public boolean validate();
	}
}
