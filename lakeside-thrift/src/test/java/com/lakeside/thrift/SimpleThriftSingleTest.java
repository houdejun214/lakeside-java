package com.lakeside.thrift;

import com.google.common.net.HostAndPort;
import com.lakeside.thrift.callers.RetryingCaller;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by dejun on 25/06/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { ThriftFactory.class, RetryingCaller.class})
@PowerMockIgnore("javax.management.*")
public class SimpleThriftSingleTest extends BaseThriftTest {

    @Test
    public void testOneServerAndAllCall() throws TException, InterruptedException {
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        Thrift<HelloClient> thrift = getIfaceThrift();
        // random call should success
        thrift.create().hi();
        server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
    }

    @Test(expected = ThriftException.class)
    public void testOneServerAndSingleCall() throws TException, InterruptedException {
        ThriftServer server2 = mockServer(7912).awaitForAlive();
        try {
            Thrift<HelloClient> thrift = getIfaceThrift();
            // random call should success
            thrift.create(HostAndPort.fromParts("localhost", 7911)).hi();
        }finally {
            server2.awaitShutdown(Amount.of(5L, Time.SECONDS));
        }
    }
}
