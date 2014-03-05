package com.lakeside.thrift.pool;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.exception.ThriftException;
import com.lakeside.thrift.host.ThriftHost;
import com.lakeside.thrift.host.ThriftHostLoader;
import com.lakeside.thrift.host.ThriftHostManager;
import com.lakeside.thrift.pool.ThriftConnection.TServiceValidator;

/**
 * @author zhufb
 *
 */
public class ThriftConnectionFactory<T extends TServiceClient & TServiceValidator> implements PoolableObjectFactory<ThriftConnection<T>> {
	
	private static final Logger log = LoggerFactory.getLogger("ThriftConnectionFactory");
    private ThriftHostManager hostManager;
	private ThriftConnectionPool<T> pool;
    public ThriftConnectionFactory(ThriftConnectionPool<T> pool,ThriftConfig cfg,ThriftHostLoader loader){
    	this.pool = pool;
    	this.hostManager = new ThriftHostManager(cfg,loader);
    }

    /**
     * @return the pool
     */
    protected ThriftConnectionPool<T> getPool() {
        return pool;
    }

	public void destroyObject(ThriftConnection<T> obj) {
		obj.destroy();
		obj = null;
	}

	public boolean validateObject(ThriftConnection<T> obj) {
		if(obj == null){
			return false;
		}
		return obj.validate();
	}

	public void activateObject(ThriftConnection<T> obj) throws Exception {
		//nothing
		
	}

	public void passivateObject(ThriftConnection<T> obj) throws Exception {
		//nothing
		
	}

	public ThriftConnection<T> makeObject() throws Exception {
		ThriftHost host = null;
		while(true){
			 try{
				 host = hostManager.get();
				 return new ThriftConnection<T>(pool, host);
			 }catch(ThriftException e){
				 log.error("This thrift server was not running " + host.getIp());
			 }catch(Exception e){
				 log.error("This thrift server make new client failed " + host.getIp());
			 }
		}
	}
}