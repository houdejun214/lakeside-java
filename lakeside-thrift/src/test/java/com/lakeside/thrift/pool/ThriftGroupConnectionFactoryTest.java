package com.lakeside.thrift.pool;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.mockito.Mockito.*;

import org.apache.commons.pool2.PooledObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.lakeside.thrift.HelloClient;
import com.lakeside.thrift.SimpleThriftGroupHostManager;
import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.host.ThriftHost;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftConnection.class,ThriftGroupConnectionFactory.class,ThriftGroupConnectionPool.class})
@PowerMockIgnore("javax.management.*")
public class ThriftGroupConnectionFactoryTest {
	
	private ThriftGroupConnectionPool<HelloClient> pool=Mockito.mock(ThriftGroupConnectionPool.class);
	private HelloClient client = Mockito.mock(HelloClient.class);
	private ThriftGroupConnectionFactory<HelloClient> factory;
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
		
		manager = spy(new SimpleThriftGroupHostManager());
		manager.addHost("host1", "host1:8080");
		manager.addHost("host2", "host2:8080");
		factory = new ThriftGroupConnectionFactory<HelloClient>(pool,new ThriftConfig(),manager);
		
	}

	@Test
	public void testMakeObject() throws Exception {
		PooledObject<ThriftConnection<HelloClient>> makeObject = factory.makeObject("host1");
		verify(manager).get("host1");
		assertNotNull(makeObject);
		assertEquals("host1",makeObject.getObject().getGroupKey());
	}
	
	@Test
	public void testMakeObjectDifferent() throws Exception {
		PooledObject<ThriftConnection<HelloClient>> makeObject1 = factory.makeObject("host1");
		verify(manager).get("host1");
		assertNotNull(makeObject1);
		assertEquals("host1",makeObject1.getObject().getGroupKey());
		
		PooledObject<ThriftConnection<HelloClient>> makeObject2 = factory.makeObject("host2");
		verify(manager).get("host2");
		assertNotNull(makeObject2);
	}
	
	@Test
	public void testDestroyObject() throws Exception {
		PooledObject<ThriftConnection<HelloClient>> makeObject = factory.makeObject("host1");
		assertNotNull(makeObject);
		ThriftConnection<HelloClient> conn = makeObject.getObject();
		assertNotNull(conn);
		conn.destroy();
		verify(pool).remove(conn);
	}
	
	@Test
	public void testValidateObject() throws Exception {
		PooledObject<ThriftConnection<HelloClient>> makeObject = factory.makeObject("host1");
		assertNotNull(makeObject);
		ThriftConnection<HelloClient> conn = makeObject.getObject();
		factory.validateObject(conn);
		verify(client).validate();
	}
}
