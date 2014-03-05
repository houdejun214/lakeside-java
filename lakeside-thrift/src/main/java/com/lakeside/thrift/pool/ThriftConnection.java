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
 * One Thrift Client represent for a thrift connection to one hbase region server.
 * The thriftClient instance should be got from pool, user can't create the instance manualy
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

	public T getClient() {
		return client;
	}

	public void close() {
		pool.put(this);
	}

	public void destroy() {
		client.getInputProtocol().getTransport().close();
		mClosed = true;
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
		return client.validate();
	}
	
	private T createThriftClient() {
		String host = regionHost.getIp();
		int port = regionHost.getPort();
		int timeout = 5*60*1000;
		TSocket socket = new TSocket(host, port,timeout);
	    TProtocol protocol = new TBinaryProtocol(socket, true, true);
		try {
			ThriftConfig cfg = pool.getCfg();
			Class<T>  type =  (Class<T>)pool.getClientClass();
			Constructor constructor = type.getConstructor(TProtocol.class,ThriftConfig.class);
			T t = (T) constructor.newInstance(protocol,cfg);
			socket.open();
			return t;
		} catch (TTransportException e) {
			throw new ThriftException("Failed to open the connection.", e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ThriftException("create thirft client failed", e);
		}
	}

	@Override
	public String toString() {
		if(regionHost!=null){
			return regionHost.getIp()+":"+this.regionHost.getPort();
		}
		return super.toString();
	}
	
	public interface TServiceValidator{
		public boolean validate();
	}
}
