package com.lakeside.thrift.host;

/**
 * 
 * represent a single thrift host.
 * 
 * @author houdejun
 *
 */
public class SingleThriftHostManager implements ThriftHostManager{
	
	private ThriftHost host= null;
	
	public SingleThriftHostManager(String address) {
		host = ThriftHost.from(address);
	}
	
	public SingleThriftHostManager(String ip,int port) {
		host = ThriftHost.from(ip,port);
	}
	
	@Override
	public ThriftHost get() {
		return host;
	}
}