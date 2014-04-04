package com.lakeside.thrift.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.lang.ref.SoftReference;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.PooledSoftReference;
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

import com.lakeside.core.utils.time.StopWatch;
import com.lakeside.thrift.HelloClient;
import com.lakeside.thrift.exception.ThriftException;
import com.lakeside.thrift.host.ThriftHost;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftConnection.class,ThriftConnectionFactory.class,ThriftConnectionPool.class})
@PowerMockIgnore("javax.management.*")
public class ThriftConnectionPoolTest {
	
	private ThriftConnectionPool<HelloClient> pool=null;
	private ThriftConnectionFactory<HelloClient> connectionFactory=Mockito.mock(ThriftConnectionFactory.class);
	private HelloClient client = Mockito.mock(HelloClient.class);
	
	@Before
	public void init() throws Exception{
		final ThriftHost host = ThriftHost.from("localhost:8080");
		// mock connection
		whenNew(ThriftConnection.class).withAnyArguments().then(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return new ThriftConnection<HelloClient>(pool, host,client);
			}
		});
		
		// mock connectionFactory
		whenNew(ThriftConnectionFactory.class).withAnyArguments().thenReturn(connectionFactory);
		when(connectionFactory.makeObject()).then(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return new PooledSoftReference<ThriftConnection<HelloClient>>(
							new SoftReference<ThriftConnection<HelloClient>>( new ThriftConnection<HelloClient>(pool, host,client)));
			}
			
		});
		
//		GenericObjectPoolConfig poolConfig;
//		GenericObjectPool<ThriftConnection<HelloClient>> objectPool = new GenericObjectPool<ThriftConnection<HelloClient>>(connectionFactory, poolConfig);
//		whenNew(GenericObjectPool.class).withAnyArguments().thenReturn(objectPool);
		
		// init pool object
		pool = new ThriftConnectionPool<HelloClient>(HelloClient.class,"localhost:8080");
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
		ThriftConnection<HelloClient> con1 = pool.get();
		assertNotNull(con1);
		
		ThriftConnection<HelloClient> con2 = pool.get();
		assertNotNull(con2);
		
		pool.put(con1);
		pool.put(con2);
		
		ThriftConnection<HelloClient> con3 = pool.get();
		assertNotNull(con3);
		pool.put(con3);
		assertEquals(2,pool.size());
		verify(connectionFactory,times(2)).makeObject();
		verify(connectionFactory,never()).validateObject(Mockito.any(ThriftConnection.class));
	}
	
	@Test
	public void testGetSingle() throws Exception {
		ThriftConnection<HelloClient> con1 = pool.get();
		assertNotNull(con1);
		pool.put(con1);
		
		ThriftConnection<HelloClient> con2 = pool.get();
		assertNotNull(con2);
		pool.put(con2);
		
		ThriftConnection<HelloClient> con3 = pool.get();
		assertNotNull(con3);
		pool.put(con3);
		assertEquals(1,pool.size());
		verify(connectionFactory,times(1)).makeObject();
		verify(connectionFactory,never()).validateObject(Mockito.any(ThriftConnection.class));
	}
	
	@Test(expected=ThriftException.class)
	public void testGetMaxActive() throws Exception {
		StopWatch watch = StopWatch.newWatch();
		for(int i=0;i<10;i++){
			ThriftConnection<HelloClient> con1 = pool.get();
			assertNotNull(con1);
		}
		assertTrue(pool.size()<=10);
		doThrow(new TTransportException()).when(connectionFactory.makeObject());
		verify(connectionFactory,times(10)).makeObject();
		verify(connectionFactory,never()).validateObject(Mockito.any(ThriftConnection.class));
		
		ThriftConnection<HelloClient> con = pool.get();
		assertTrue(watch.getTime()>20000);
	}
	
	@Test
	public void testGetMaxActiveException() throws Exception {
		StopWatch watch = StopWatch.newWatch();
		when(connectionFactory.makeObject()).then(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				throw new TTransportException();
			}
		});
		ThriftConnection<HelloClient> con = pool.get();
		assertTrue(watch.getTime()>20000);
	}

	@Test
	public void testPut() {
		ThriftConnection<HelloClient> con = pool.get();
		pool.put(con);
		assertEquals(1,pool.size());
	}

	@Test
	public void testRemove() throws Exception {
		ThriftConnection<HelloClient> con = pool.get();
		assertNotNull(con);
		pool.remove(con);
		
		
		verify(connectionFactory).makeObject();
		verify(connectionFactory).destroyObject(Mockito.any(PooledObject.class));;
	}

	@Test
	public void testDestroy() throws Exception {
		ThriftConnection<HelloClient> con = pool.get();
		assertNotNull(con);
		pool.remove(con);
		verify(connectionFactory).makeObject();
		verify(connectionFactory).destroyObject(Mockito.any(PooledObject.class));;
	}
}
