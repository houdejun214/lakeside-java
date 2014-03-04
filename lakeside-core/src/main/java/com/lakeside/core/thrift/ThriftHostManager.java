package com.lakeside.core.thrift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhufb
 *
 */
public class ThriftHostManager {

	private List<ThriftHost> hosts = new ArrayList<ThriftHost>();
	private AtomicInteger next;
	private ThriftHostLoader thriftLoader;
	public ThriftHostManager(ThriftConfig cfg,ThriftHostLoader loader){
		this.thriftLoader = loader;
		this.hosts = thriftLoader.load(cfg);
	}

	public ThriftHost getRandomHost(){
		Long index =  Math.round(Math.random() * (hosts.size() - 1));
		return hosts.get(index.intValue());
	}
	
	public int size(){
		return hosts.size();
	}
	
	public ThriftHost get(){
		if(next == null){
			synchronized (this) {
				if(next == null){
					next = new AtomicInteger(random());
				}
			}
		}
		List<ThriftHost> list = list();
		return list.get(next.getAndIncrement() % list.size());
	}

	public List<ThriftHost> list(){
		return hosts;
	}
	
	private int random(){
		return ((Long)Math.round(Math.random() * (size() - 1))).intValue();
	}
}
