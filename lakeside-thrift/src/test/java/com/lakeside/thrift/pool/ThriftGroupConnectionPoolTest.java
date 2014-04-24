package com.lakeside.thrift.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.lang.ref.SoftReference;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.PooledSoftReference;
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
import com.lakeside.thrift.SimpleThriftGroupHostManager;
import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.ThriftException;
import com.lakeside.thrift.host.ThriftHost;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftConnection.class,ThriftGroupConnectionFactory.class,ThriftGroupConnectionPool.class})
@PowerMockIgnore("javax.management.*")
public class ThriftGroupConnectionPoolTest {
	
	private ThriftGroupConnectionPool<HelloClient> pool=null;
	private ThriftGroupConnectionFactory<HelloClient> connectionFactory=Mockito.mock(ThriftGroupConnectionFactory.class);
	private HelloClient client = Mockito.mock(HelloClient.class);
	private SimpleThriftGroupHostManager manager;
	
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
		whenNew(ThriftGroupConnectionFactory.class).withAnyArguments().thenReturn(connectionFactory);
		when(connectionFactory.makeObject(anyString())).then(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String key = (String) invocation.getArguments()[0];
				ThriftConnection<HelloClient> conn = new ThriftConnection<HelloClient>(pool, host,client);
				conn.setGroupKey(key);
				return new PooledSoftReference<ThriftConnection<HelloClient>>(
							new SoftReference<ThriftConnection<HelloClient>>( conn));
			}
			
		});
		
		manager = new SimpleThriftGroupHostManager();
		manager.addHost("host1", "host1:8080");
		manager.addHost("host2", "host2:8080");
		// init pool object
		pool = new ThriftGroupConnectionPool<HelloClient>(HelloClient.class,manager);
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
		ThriftConnection<HelloClient> con1 = pool.get("host1");
		assertNotNull(con1);
		assertEquals("host1",con1.getGroupKey());
		ThriftConnection<HelloClient> con2 = pool.get("host2");
		assertNotNull(con2);
		assertEquals("host2",con2.getGroupKey());
		
		pool.put(con1);
		pool.put(con2);
		
		ThriftConnection<HelloClient> con3 = pool.get("host1");
		assertNotNull(con3);
		pool.put(con3);
		assertEquals(2,pool.size());
		verify(connectionFactory,times(1)).makeObject("host1");
		verify(connectionFactory,times(1)).makeObject("host2");
		verify(connectionFactory,never()).validateObject(Mockito.any(PooledObject.class));
	}
	
	@Test
	public void testGetSingle() throws Exception {
		ThriftConnection<HelloClient> con1 = pool.get("host1");
		assertNotNull(con1);
		pool.put(con1);
		
		ThriftConnection<HelloClient> con2 = pool.get("host1");
		assertNotNull(con2);
		pool.put(con2);
		
		ThriftConnection<HelloClient> con3 = pool.get("host1");
		assertNotNull(con3);
		pool.put(con3);
		assertEquals(1,pool.size());
		verify(connectionFactory,times(1)).makeObject(anyString());
		verify(connectionFactory,never()).validateObject(Mockito.any(PooledObject.class));
	}
	
	@Test(expected=ThriftException.class)
	public void testGetMaxActive() throws Exception {
		ThriftConfig conf = new ThriftConfig();
		conf.put("thrift.pool.maxWait","2000");
		pool = new ThriftGroupConnectionPool<HelloClient>(HelloClient.class,conf,manager);
		StopWatch watch = StopWatch.newWatch();
		for(int i=0;i<10;i++){
			ThriftConnection<HelloClient> con1 = pool.get("host1");
			assertNotNull(con1);
		}
		assertTrue(pool.size()<=10);
		verify(connectionFactory,times(10)).makeObject(anyString());
		verify(connectionFactory,never()).validateObject(Mockito.any(PooledObject.class));
		
		ThriftConnection<HelloClient> con = pool.get("host1");
		assertTrue(watch.getTime()>2000);
	}

	@Test
	public void testPut() {
		ThriftConnection<HelloClient> con = pool.get("host1");
		pool.put(con);
		assertEquals(1,pool.size());
	}

	@Test
	public void testRemove() throws Exception {
		ThriftConnection<HelloClient> con = pool.get("host1");
		assertNotNull(con);
		pool.remove(con);
		
		verify(connectionFactory).makeObject("host1");
		verify(connectionFactory).destroyObject(anyString(),Mockito.any(PooledObject.class));;
	}

	@Test
	public void testDestroy() throws Exception {
		ThriftConnection<HelloClient> con = pool.get("host1");
		assertNotNull(con);
		pool.remove(con);
		verify(connectionFactory).makeObject(anyString());
		verify(connectionFactory).destroyObject(anyString(),Mockito.any(PooledObject.class));;
	}
}
