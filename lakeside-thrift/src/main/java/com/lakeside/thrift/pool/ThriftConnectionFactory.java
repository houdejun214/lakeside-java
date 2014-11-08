package com.lakeside.thrift.pool;

import com.google.common.net.HostAndPort;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.PooledSoftReference;
import org.apache.thrift.TServiceClient;

import java.lang.ref.SoftReference;

/**
 * Grouped ThriftConnection object Factory,
 * 用于创建一个到服务器的连接（thrift socket connection).
 * 
 * @author houdejun
 */
public class ThriftConnectionFactory<T extends TServiceClient & ThriftConnection.TServiceValidator> extends BaseKeyedPooledObjectFactory<HostAndPort,ThriftConnection<T>> {
	
	private ThriftConnectionPool<T> pool;


    public ThriftConnectionFactory(){
    }

    public ThriftConnectionPool<T> getPool() {
        return pool;
    }

    public void setPool(ThriftConnectionPool<T> pool) {
        this.pool = pool;
    }

    /**
     * 关闭一个连接对象
     * @param obj
     */
	public void destroyObject(PooledObject<ThriftConnection<T>> obj) {
		obj.getObject().destroy();
	}
	
	/**
	 * 校验连接对象(ThriftConnection)是否可用
	 * @param obj
	 * @return
	 */
	public boolean validateObject(PooledObject<ThriftConnection<T>> obj) {
		if(obj == null){
			return false;
		}
		return obj.getObject().validate();
	}

    @Override
    public boolean validateObject(HostAndPort key, PooledObject<ThriftConnection<T>> p) {
        if(p == null){
            return false;
        }
        return  p.getObject().validate();
    }

    @Override
	public ThriftConnection<T> create(HostAndPort endpoint) throws Exception {
		return new ThriftConnection<>(pool,endpoint);
	}

	@Override
	public PooledObject<ThriftConnection<T>> wrap(ThriftConnection<T> obj) {
		return new PooledSoftReference<>(new SoftReference<>(obj));
	}
}