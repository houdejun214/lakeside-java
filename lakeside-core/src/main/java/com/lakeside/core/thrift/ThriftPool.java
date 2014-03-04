package com.lakeside.core.thrift;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.thrift.TServiceClient;

import com.lakeside.core.thrift.ThriftConnection.TServiceValidator;

/**
 * @author zhufb
 *
 */
public abstract class ThriftPool {

	static {
		Runtime runtime = Runtime.getRuntime();
		Class<? extends Runtime> c = runtime.getClass();
		try {
			Method m = c.getMethod("addShutdownHook",
					new Class[] { Thread.class });
			m.invoke(runtime, new Object[] { new ShutdownThread() });
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<ThriftConfig,ThriftConnectionPool<?>> poolMap = new HashMap<ThriftConfig,ThriftConnectionPool<?>>();
	
	/**
	 *  get thrift pool with thrift client class,thrift config,thrift host loader
	 *
	 * @param cls  thrift client class (custom)
	 * @param cfg  thrift pool config info
	 * @param loader thrift host list loader instance (custom)
	 * @return
	 */
	public static <T extends TServiceClient & TServiceValidator> ThriftConnectionPool<T> get(Class<T> cls,ThriftConfig cfg,ThriftHostLoader loader){
		if(!poolMap.containsKey(cfg)){
			synchronized (poolMap) {
				if(!poolMap.containsKey(cfg)){
					poolMap.put(cfg,new ThriftConnectionPool<T>(cls,cfg,loader));
				}
			}
		}
		return (ThriftConnectionPool<T>) poolMap.get(cfg);
	}

	private static class ShutdownThread extends Thread {
		public void run() {
			try {
				Map<ThriftConfig,ThriftConnectionPool<?>> pools = poolMap;
				if(pools == null){
					return;
				}
				Iterator<ThriftConnectionPool<?>> iterator = pools.values().iterator();
				while(iterator.hasNext()){
					iterator.next().destroy();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
