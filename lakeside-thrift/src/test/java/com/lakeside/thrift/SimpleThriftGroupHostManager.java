package com.lakeside.thrift;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.lakeside.thrift.host.ThriftGroupHostManager;
import com.lakeside.thrift.host.ThriftHost;

public class SimpleThriftGroupHostManager implements ThriftGroupHostManager{

	private Map<String,Set<ThriftHost>> hosts = new HashMap<>();
	
	public void addHost(String key,ThriftHost host){
		Set<ThriftHost> set = hosts.get(key);
		if(set==null){
			set = Sets.newHashSet();
			hosts.put(key, set);
		}
		set.add(host);
	}
	
	public void addHost(String key,String host,int port){
		Set<ThriftHost> set = hosts.get(key);
		if(set==null){
			set = Sets.newHashSet();
			hosts.put(key, set);
		}
		set.add(ThriftHost.from(host, port));
	}
	
	public void addHost(String key,String address){
		Set<ThriftHost> set = hosts.get(key);
		if(set==null){
			set = Sets.newHashSet();
			hosts.put(key, set);
		}
		set.add(ThriftHost.from(address));
	}
	
	public ThriftHost get(String key){
		Set<ThriftHost> set = hosts.get(key);
		if(set==null){
			return null;
		}
		return set.iterator().next();
	}
}
