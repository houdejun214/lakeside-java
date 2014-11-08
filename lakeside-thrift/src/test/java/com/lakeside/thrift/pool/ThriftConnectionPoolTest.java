package com.lakeside.thrift.pool;

import com.google.common.net.HostAndPort;
import com.lakeside.core.utils.time.StopWatch;
import com.lakeside.thrift.HelloClient;
import com.lakeside.thrift.ThriftException;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.PooledSoftReference;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.SoftReference;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftConnection.class,ThriftConnectionFactory.class,ThriftConnectionPool.class})
@PowerMockIgnore("javax.management.*")
public class ThriftConnectionPoolTest {
	
	private ThriftConnectionPool<HelloClient> pool=null;
	private ThriftConnectionFactory<HelloClient> connectionFactory= null;

	@Before
	public void init() throws Exception{
        connectionFactory= Mockito.mock(ThriftConnectionFactory.class);
		final HostAndPort host = HostAndPort.fromString("localhost:8080");
		// mock connection
		whenNew(ThriftConnection.class).withAnyArguments().then(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return new ThriftConnection<>(pool, host);
			}
		});
		
		// mock connectionFactory
		whenNew(ThriftConnectionFactory.class).withAnyArguments().thenReturn(connectionFactory);
		when(connectionFactory.makeObject(any(HostAndPort.class))).then(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return new PooledSoftReference<>(
							new SoftReference<>( new ThriftConnection<>(pool, host)));
			}
			
		});
        TSocket transport = Mockito.mock(TSocket.class);
        whenNew(TSocket.class).withAnyArguments().thenReturn(transport);
		// init pool object
		pool = new ThriftConnectionPool<>(HelloClient.class);
	}
	
	@After
	public void close(){
		pool.destroy();
	}
	
	@Test
	public void testThriftConnectionPoolInit() {
		assertEquals(0,pool.size());
	}

	@Test
	public void testGet() throws Exception {
		ThriftConnection<HelloClient> con1 = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con1);
		
		ThriftConnection<HelloClient> con2 = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con2);
		
		pool.put(con1);
		pool.put(con2);
		
		ThriftConnection<HelloClient> con3 = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con3);
		pool.put(con3);
		assertEquals(2,pool.size());
		verify(connectionFactory,times(2)).makeObject(HostAndPort.fromString("localhost:8080"));
		verify(connectionFactory,never()).validateObject(Mockito.any(PooledObject.class));
	}
	
	@Test
	public void testGetSingle() throws Exception {
		ThriftConnection<HelloClient> con1 = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con1);
		pool.put(con1);
		
		ThriftConnection<HelloClient> con2 = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con2);
		pool.put(con2);
		
		ThriftConnection<HelloClient> con3 = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con3);
		pool.put(con3);
		assertEquals(1,pool.size());
		verify(connectionFactory,times(1)).makeObject(HostAndPort.fromString("localhost:8080"));
		verify(connectionFactory,never()).validateObject(Mockito.any(PooledObject.class));
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testGetMaxActive() throws Exception {
		ThriftPoolConfig conf = new ThriftPoolConfig();
		conf.put("thrift.pool.maxWait","2000");
		pool = new ThriftConnectionPool<>(HelloClient.class,conf);
		StopWatch watch = StopWatch.newWatch();
		for(int i=0;i<10;i++){
			ThriftConnection<HelloClient> con1 = pool.get(HostAndPort.fromString("localhost:8080"));
			assertNotNull(con1);
		}
		assertTrue(pool.size()<=10);
		verify(connectionFactory,times(10)).makeObject(HostAndPort.fromString("localhost:8080"));
		verify(connectionFactory,never()).validateObject(Mockito.any(PooledObject.class));
		
		ThriftConnection<HelloClient> con = pool.get(HostAndPort.fromString("localhost:8080"));
		assertTrue(watch.getTime()>2000);
	}
	
	@Test(expected=ThriftException.class)
	public void testGetMaxActiveException() throws Exception {
		StopWatch watch = StopWatch.newWatch();
		when(connectionFactory.makeObject(HostAndPort.fromString("localhost:8080"))).then(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				throw new TTransportException();
			}
		});
		ThriftConnection<HelloClient> con = pool.get(HostAndPort.fromString("localhost:8080"));
		assertTrue(watch.getTime()>20000);
	}

	@Test
	public void testPut() throws Exception {
		ThriftConnection<HelloClient> con = pool.get(HostAndPort.fromString("localhost:8080"));
		pool.put(con);
		assertEquals(1,pool.size());
	}

	@Test
	public void testValidate() throws Exception {
        HostAndPort endpoint = HostAndPort.fromString("localhost:8080");
        ThriftConnection<HelloClient> con = pool.get(endpoint);
		assertNotNull(con);
		pool.put(con);
		pool.getGenericPool().evict();
		verify(connectionFactory).makeObject(endpoint);
		verify(connectionFactory).validateObject(Mockito.any(HostAndPort.class), Mockito.any(PooledObject.class));
	}
	
	@Test
	public void testRemove() throws Exception {
		ThriftConnection<HelloClient> con = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con);
		pool.put(con);
		pool.remove(con);
		assertEquals(0,pool.size());
		verify(connectionFactory).makeObject(HostAndPort.fromString("localhost:8080"));
		verify(connectionFactory).destroyObject(Mockito.any(HostAndPort.class),Mockito.any(PooledObject.class));;
	}

	@Test
	public void testDestroy() throws Exception {
		ThriftConnection<HelloClient> con = pool.get(HostAndPort.fromString("localhost:8080"));
		assertNotNull(con);
		con.destroy();
		assertEquals(0,pool.size());
        
		verify(connectionFactory).makeObject(HostAndPort.fromString("localhost:8080"));
		verify(connectionFactory).destroyObject(Mockito.any(HostAndPort.class),Mockito.any(PooledObject.class));
	}
}
