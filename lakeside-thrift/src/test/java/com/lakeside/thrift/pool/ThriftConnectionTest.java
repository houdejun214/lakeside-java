package com.lakeside.thrift.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.mockito.Mockito;

import com.lakeside.thrift.HelloClient;
import com.lakeside.thrift.host.ThriftHost;

@SuppressWarnings("unchecked")
public class ThriftConnectionTest {
	
	ThriftConnectionPool<HelloClient> pool = Mockito.mock(ThriftConnectionPool.class);
	HelloClient client = Mockito.mock(HelloClient.class);
	ThriftHost host = ThriftHost.from("localhost:8080");
	

	@Test
	public void testThriftConnection() throws Exception {
		ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
		assertEquals(client,conn.getClient());
	}

	@Test
	public void testClose() {
		ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
		conn.close();
		verify(pool).put(conn);
	}

	@Test
	public void testDestroy() {
		TProtocol prot = Mockito.mock(TProtocol.class);
		when(prot.getTransport()).thenReturn(Mockito.mock(TTransport.class));
		when(client.getInputProtocol()).thenReturn(prot);
		
		ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
		conn.destroy();
		verify(pool).remove(conn);
	}

	@Test
	public void testValidate() {
		
		ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
		when(client.validate()).thenReturn(true);
		assertTrue(conn.validate());
		verify(client).validate();
	}
	
	@Test
	public void testValidateWithException() {
		ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
		when(client.validate()).thenThrow(Exception.class);
		assertFalse(conn.validate());
	}

	@Test
	public void testToString() {
		ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
		assertEquals("localhost:8080",conn.toString());
	}

}
