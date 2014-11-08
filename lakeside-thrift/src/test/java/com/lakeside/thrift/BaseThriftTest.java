package com.lakeside.thrift;

import com.google.common.collect.ImmutableSet;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

/**
 * Created by dejun on 29/10/14.
 */
public class BaseThriftTest {
    protected ThriftServer mockServer(final int port) throws TTransportException {
        Hello.Processor<Hello.Iface> processor = new Hello.Processor<Hello.Iface>(new Hello.Iface(){
                        @Override
            public String hi() throws TException {
                System.out.println("say hi on "+port);
                return String.valueOf(port);
            }
        });
        ThriftServer server = new FakeServer();
        server.start(port,processor);
        return server;
    }

    protected Thrift<HelloClient> getIfaceThrift() {
        DynamicHostSet sets = new DynamicHostSet();
        sets.addServerInstance("localhost", 7911);
        sets.addServerInstance("localhost", 7912);
        return getIfaceThrift(sets);
    }

    protected Thrift<HelloClient> getIfaceThrift(DynamicHostSet backends) {
        Config thriftConfig = Config.builder()
                .withRequestTimeout(Amount.of(0L, Time.SECONDS))
                .withRetries(3)
                .disableStats()
                .withDebug(true)
                .retryOn(ImmutableSet.<Class<? extends Exception>>builder()
                        .add(ThriftException.class)
                        .add(IOException.class)
                        .add(ConnectFailedException.class)
                        .add(TTimeoutException.class)
                        .add(ResourceExhaustedException.class)
                        .add(TTransportException.class).build())
                .create();
        try {
            return ThriftFactory.create(HelloClient.class)
                    .useFramedTransport(true)
                    .withMaxConnectionsPerEndpoint(5)
                    .withThriftConfig(thriftConfig)
                    .build(backends);
        } catch (Exception e) {
            return null;
        }
    }

    protected void assertHit(ConcurrentMap<String, AtomicLong> hits, String key) {
        assertTrue(hits.get(key) != null && hits.get(key).get() >= 1);
    }

    protected void assertNotHit(ConcurrentMap<String, AtomicLong> hits, String key) {
        assertTrue(hits.get(key) == null || hits.get(key).get() == 0);
    }
}
