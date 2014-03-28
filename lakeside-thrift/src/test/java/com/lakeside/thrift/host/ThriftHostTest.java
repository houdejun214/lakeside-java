package com.lakeside.thrift.host;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThriftHostTest {

	@Test
	public void testThriftHost() {
		ThriftHost host = new ThriftHost("127.0.0.1",8080);
		assertEquals("127.0.0.1",host.getIp());
		assertEquals(8080,host.getPort());
	}

	@Test
	public void testFrom() {
		ThriftHost host = ThriftHost.from("127.0.0.1",8080);
		assertEquals("127.0.0.1",host.getIp());
		assertEquals(8080,host.getPort());
	}

	@Test
	public void testFromString() {
		ThriftHost host = ThriftHost.from("127.0.0.1:8080");
		assertEquals("127.0.0.1",host.getIp());
		assertEquals(8080,host.getPort());
		
		host = ThriftHost.from("localhost:8080");
		assertEquals("localhost",host.getIp());
		assertEquals(8080,host.getPort());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromStringException() {
		ThriftHost host = ThriftHost.from("127.0.0.1");
		assertEquals("127.0.0.1",host.getIp());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromStringExceptionOther() {
		ThriftHost host = ThriftHost.from("localhost");
		assertEquals("127.0.0.1",host.getIp());
	}
}
