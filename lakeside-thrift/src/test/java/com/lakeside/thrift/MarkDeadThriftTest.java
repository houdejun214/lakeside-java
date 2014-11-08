package com.lakeside.thrift;

import com.google.common.collect.Maps;
import com.lakeside.thrift.callers.RetryingCaller;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by dejun on 25/06/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftFactory.class, RetryingCaller.class,LoggerFactory.class })
@PowerMockIgnore("javax.management.*")
public class MarkDeadThriftTest extends BaseThriftTest {

    @Test
    public void testTwoServer() throws TException, InterruptedException {
        ThriftServer server1 = mockServer(7911).awaitForAlive();
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        Logger logger = mock(Logger.class);
        PowerMockito.mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);
        when(LoggerFactory.getLogger(any(String.class))).thenReturn(logger);
        Thrift<HelloClient> thrift = getIfaceThrift();
        Hello.Iface iface = thrift.create();
        ConcurrentMap<String, AtomicLong> hits = Maps.newConcurrentMap();


        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        assertHit(hits, "7911");
        assertHit(hits, "7912");
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));

        hits = Maps.newConcurrentMap();
        for (int i = 0; i < 4; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        verify(logger).warn(eq("add [{}] to dead list"),anyObject());
        reset(logger);
        Thread.sleep(11*1000);
        verify(logger).warn(eq("try to add dead[{}] to live list"),anyObject());
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        verify(logger).warn(eq("add [{}] to dead list"),anyObject());
        assertHit(hits, "7911");
        assertNotHit(hits, "7912");
        System.out.println(hits);
        server1.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }


}
