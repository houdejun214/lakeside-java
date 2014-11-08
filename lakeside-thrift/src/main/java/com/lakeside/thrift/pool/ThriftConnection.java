package com.lakeside.thrift.pool;

import com.google.common.net.HostAndPort;
import com.lakeside.thrift.ConnectFailedException;
import com.lakeside.thrift.ThriftException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * A Thrift Connection represent for a thrift connection (thrift socket) to a thrift server.It must be bound with a TServiceClient.
 * 
 *
 */
public class ThriftConnection<T extends TServiceClient & ThriftConnection.TServiceValidator> {
    private static final Logger log = LoggerFactory.getLogger(ThriftConnection.class);
    private boolean mClosed = false;
    private final TProtocol protocol;
    private final ThriftConnectionPool<T> pool;
    private boolean compact = true;
    private boolean framed = true;
    private final int socketTimeout;
    private HostAndPort endpoint;
    private TTransport transport;

    public ThriftConnection(ThriftConnectionPool<T> pool, HostAndPort rh) throws ThriftException {
		this.pool = pool;
		this.endpoint = rh;
		this.compact = pool.getCfg().getBoolean("thrift.pool.protocol.compact", false);
		this.framed = pool.getCfg().getBoolean("thrift.pool.transport.framed", false);
		this.socketTimeout = pool.getCfg().getInt("thrift.pool.transport.socket_timeout", 5*60*1000);
		this.protocol = newTProtocol();
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
        try {
            if(!mClosed){
                if (this.protocol != null) {
                    TTransport transport = this.protocol.getTransport();
                    if (transport != null) transport.close();
                }
            }
        } catch (Exception e) {
            log.warn("Destroy ThriftConnection failed, ", e.getMessage());
        } finally {
            if(!mClosed) {
                pool.remove(this);
            }
            mClosed = true;
        }
    }

	/**
	 * validate the connection is available
	 * @return the closed
	 * @throws java.io.IOException
	 */
	public boolean validate()  {
		if(mClosed){
			return false;
		}
		try{
			return protocol.getTransport().isOpen();
		} catch (Exception e){
			return false;
		}
	}
	
	/**
	 * create thrift client
	 * @return
	 */
	private TProtocol newTProtocol() throws ThriftException {
        try {
            String host = endpoint.getHostText();
            int port = endpoint.getPort();
            transport = new TSocket(host, port, socketTimeout);
            if(framed){
                transport = new TFramedTransport(transport);
            }
            TProtocol protocol = compact?new TCompactProtocol(transport):new TBinaryProtocol(transport);
            protocol.getTransport().open();
            return protocol;
        } catch (TTransportException e) {
            throw new ConnectFailedException("Failed to open the connection to "+this.endpoint, e);
        } catch (Exception e) {
            throw new ThriftException("create thirft client failed", e);
        }
	}

	@Override
	public String toString() {
		return this.endpoint.toString();
	}
	
    public HostAndPort getEndpoint() {
        return endpoint;
    }

    public TTransport get() {
        return this.transport;

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
