package com.lakeside.thrift;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.transport.TTransportException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.lakeside.thrift.ThriftTemplate.ThriftAction;
import com.lakeside.thrift.ThriftTemplate.ThriftActionNoResult;
import com.lakeside.thrift.pool.ThriftConnection;
import com.lakeside.thrift.pool.ThriftConnectionFactory;
import com.lakeside.thrift.pool.ThriftConnectionPool;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftConnection.class,ThriftConnectionFactory.class,ThriftConnectionPool.class})
@PowerMockIgnore("javax.management.*")
public class ThriftTemplateTest {
	
	private ThriftConnectionPool<HelloClient> pool=Mockito.mock(ThriftConnectionPool.class);
	
	@Before
	public void init() throws Exception{
	}

	@Test
	public void testExecuteThriftActionOfTR() {
		
		ThriftConnection<HelloClient> conn=Mockito.mock(ThriftConnection.class);
		HelloClient client = Mockito.mock(HelloClient.class);
		when(pool.get()).thenReturn(conn);
		when(conn.getClient()).thenReturn(client);
		
		ThriftTemplate<HelloClient> template = new ThriftTemplate<HelloClient>(pool); 
		int ret = template.execute(new ThriftAction<HelloClient,Integer>(){
			@Override
			public Integer action(HelloClient client) throws Exception {
				return 1;
			}
		});
		verify(pool,times(1)).get();
		verify(conn).close();
		assertEquals(1,ret);
	}

	@Test
	public void testExecuteThriftActionNoResultOfT() {
		ThriftConnection<HelloClient> conn=Mockito.mock(ThriftConnection.class);
		HelloClient client = Mockito.mock(HelloClient.class);
		when(pool.get()).thenReturn(conn);
		when(conn.getClient()).thenReturn(client);
		
		ThriftTemplate<HelloClient> template = new ThriftTemplate<HelloClient>(pool); 
		template.execute(new ThriftActionNoResult<HelloClient>(){
			@Override
			public void action(HelloClient client) throws Exception {
				
			}
		});
		verify(pool,times(1)).get();
		verify(conn).close();
	}
	
	@Test
	public void testExecuteThriftActionNoResultWithExceptionRetry() {
		ThriftConnection<HelloClient> conn=Mockito.mock(ThriftConnection.class);
		HelloClient client = Mockito.mock(HelloClient.class);
		when(pool.get()).thenReturn(conn);
		when(conn.getClient()).thenReturn(client);
		final AtomicInteger counter = new AtomicInteger(0);
		
		ThriftTemplate<HelloClient> template = new ThriftTemplate<HelloClient>(pool); 
		template.execute(new ThriftActionNoResult<HelloClient>(){
			@Override
			public void action(HelloClient client) throws Exception {
				if(counter.get()==0){
					counter.set(counter.get()+1);
					throw new TTransportException();
				}
			}
		});
		verify(pool,times(2)).get();
		verify(conn).destroy();
		verify(conn,times(2)).close();
	}

}
