package com.lakeside.thrift.pool;

import com.google.common.net.HostAndPort;
import com.lakeside.thrift.HelloClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftConnection.class,ThriftConnectionPool.class})
@PowerMockIgnore("javax.management.*")
public class ThriftConnectionTest {

    ThriftConnectionPool<HelloClient> pool = null;
    TSocket transport = null;
    HelloClient client = null;
    HostAndPort host = null;

    @Before
    public void setup() throws Exception {
        pool = PowerMockito.spy(new ThriftConnectionPool(HelloClient.class));
        transport = Mockito.mock(TSocket.class);
        client = Mockito.mock(HelloClient.class);
        host = HostAndPort.fromString("localhost:8080");
        whenNew(TSocket.class).withAnyArguments().thenReturn(transport);
    }

    @Test
    public void testThriftConnection() throws Exception {
        ThriftConnection<HelloClient> conn = new ThriftConnection<>(pool, host);
        assertEquals(transport, conn.get());
    }

    @Test
    public void testClose() {
        ThriftConnection<HelloClient> conn = new ThriftConnection<>(pool, host);
        conn.close();
        verify(pool).put(conn);
    }

    @Test
    public void testDestroy() {
        TProtocol prot = Mockito.mock(TProtocol.class);
        when(prot.getTransport()).thenReturn(Mockito.mock(TTransport.class));
        when(client.getInputProtocol()).thenReturn(prot);

        ThriftConnection<HelloClient> conn = new ThriftConnection<>(pool, host);
        conn.destroy();
        verify(pool).remove(conn);
    }

    @Test
    public void testValidate() {
        ThriftConnection<HelloClient> conn = new ThriftConnection<>(pool, host);
        when(transport.isOpen()).thenReturn(true);
        assertTrue(conn.validate());
    }

    @Test
    public void testValidateWithException() {
        ThriftConnection<HelloClient> conn = new ThriftConnection<>(pool, host);
        when(transport.isOpen()).thenReturn(false);
        assertFalse(conn.validate());
    }

    @Test
    public void testToString() {
        ThriftConnection<HelloClient> conn = new ThriftConnection<>(pool, host);
        assertEquals("localhost:8080", conn.toString());
    }

}
