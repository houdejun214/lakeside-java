package com.lakeside.thrift;

import com.google.common.collect.Maps;
import com.lakeside.thrift.callers.RetryingCaller;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * Created by dejun on 25/06/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftFactory.class, RetryingCaller.class})
@PowerMockIgnore("javax.management.*")
public class SimpleThriftTest extends BaseThriftTest {

    @Test(expected = ResourceExhaustedException.class)
    public void testNoneServer() throws TException {
        Thrift<HelloClient> thrift = getIfaceThrift();
        thrift.create().hi();
    }

    @Test
    public void testSingleCall() throws TException, InterruptedException {
        ThriftServer server = mockServer(7911);
        server.awaitForAlive();
        Thrift<HelloClient> thrift = getIfaceThrift();
        thrift.create().hi();
        server.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testOneServer() throws TException, InterruptedException {
        ThriftServer server = mockServer(7911);
        server.awaitForAlive();
        Thrift<HelloClient> thrift = getIfaceThrift();
        Hello.Iface iface = thrift.create();
        for (int i = 0; i < 10; i++) {
            iface.hi();
        }
        server.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testTwoServer() throws TException, InterruptedException {
        ThriftServer server1 = mockServer(7911).awaitForAlive();
        ThriftServer server2 = mockServer(7912).awaitForAlive();
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
        server1.awaitShutdown(Amount.of(5L, Time.SECONDS));
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testTwoServerAndInterrupt() throws TException, InterruptedException {
        ThriftServer server1 = mockServer(7911).awaitForAlive();
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        Thrift<HelloClient> thrift = getIfaceThrift();
        Hello.Iface iface = thrift.create();
        ConcurrentMap<String, AtomicLong> hits = Maps.newConcurrentMap();
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        assertTrue(hits.get("7911").get() >= 1);
        assertTrue(hits.get("7912").get() >= 1);

        System.out.println(hits);
        server1.awaitShutdown(Amount.of(5L, Time.SECONDS));
        hits = Maps.newConcurrentMap();
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        assertHit(hits, "7912");
        assertNotHit(hits, "7911");
        System.out.println(hits);
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testDynamicAdd() throws TException, InterruptedException {
        DynamicHostSet backends = new DynamicHostSet();
        backends.addServerInstance("localhost", 7911);
        Thrift<HelloClient> thrift = getIfaceThrift(backends);
        Hello.Iface iface = thrift.create();
        ThriftServer server1 = mockServer(7911).awaitForAlive();
        ConcurrentMap<String, AtomicLong> hits = Maps.newConcurrentMap();
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }

        assertHit(hits, "7911");
        assertNotHit(hits, "7912");

        backends.addServerInstance("localhost", 7912);
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        hits = Maps.newConcurrentMap();
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        assertHit(hits, "7912");
        assertHit(hits, "7911");
        System.out.println(hits);
        server1.awaitShutdown(Amount.of(5L, Time.SECONDS));
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testDynamicDiedServer() throws TException, InterruptedException {
        DynamicHostSet backends = new DynamicHostSet();
        backends.addServerInstance("localhost", 7911);
        backends.addServerInstance("localhost", 7912);
        Thrift<HelloClient> thrift = getIfaceThrift(backends);
        Hello.Iface iface = thrift.create();
        ThriftServer server1 = mockServer(7911).awaitForAlive();
        ThriftServer server2 = mockServer(7912).awaitForAlive();
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
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        assertHit(hits, "7911");
        assertNotHit(hits, "7912");
        System.out.println(hits);
        server1.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testDynamicRemove() throws TException, InterruptedException {
        DynamicHostSet backends = new DynamicHostSet();
        backends.addServerInstance("localhost", 7911);
        backends.addServerInstance("localhost", 7912);
        Thrift<HelloClient> thrift = getIfaceThrift(backends);
        Hello.Iface iface = thrift.create();
        ThriftServer server1 = mockServer(7911).awaitForAlive();
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        ConcurrentMap<String, AtomicLong> hits = Maps.newConcurrentMap();
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }

        assertHit(hits, "7911");
        assertHit(hits, "7912");
        backends.removeServerInstance("localhost", 7912);
        hits = Maps.newConcurrentMap();
        for (int i = 0; i < 10; i++) {
            String port = iface.hi();
            assertNotNull(port);
            hits.putIfAbsent(port, new AtomicLong(0));
            hits.get(port).incrementAndGet();
        }
        assertHit(hits, "7911");
        assertNotHit(hits, "7912");
        System.out.println(hits);
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
        server1.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test
    public void testExceptionRevert() throws TException, InterruptedException {
        Thrift<HelloClient> thrift = getIfaceThrift();
        Hello.Iface iface = thrift.create();
        try {
            iface.hi();
            fail("Should have thrown TResourceExhaustedException");
        } catch (ResourceExhaustedException|ConnectFailedException e) {
            assertNotNull(e);
        } catch (Exception e) {
            fail("Should have thrown TResourceExhaustedException");
        }
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        Thread.sleep(1000*11);
        assertEquals("7912", iface.hi());
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }
}
