package com.lakeside.data.mongo;


import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { MongoDataSource.class})
@PowerMockIgnore("javax.management.*")
public class MongoDataSourceTest {

    @Test
    public void testInitDb() throws Exception {
        MongoClient client = Mockito.mock(MongoClient.class);
        whenNew(MongoClient.class).withAnyArguments().thenReturn(client);
        MongoDataSource source = new MongoDataSource();
        source.setHost("localhost");
        source.setDefaultDBName("test");
        source.initDb();
        assertNotNull(source.getM());
        verifyNew(MongoClient.class).withArguments("localhost",27017);
    }

    @Test
    public void testInitDbWithUserName() throws Exception {
        MongoClient client = Mockito.mock(MongoClient.class);
        whenNew(MongoClient.class).withAnyArguments().thenReturn(client);
        MongoDataSource source = new MongoDataSource();
        source.setHost("localhost");
        source.setDefaultDBName("test");
        source.setUserName("user");
        source.setPassword("pwd");
        source.setDefaultDBName("db");
        source.initDb();
        assertNotNull(source.getM());
        MongoCredential credential = MongoCredential.createMongoCRCredential("user", "db", "pwd".toCharArray());
        verifyNew(MongoClient.class).withArguments(new ServerAddress("localhost", 27017) , Arrays.asList(credential));
    }
}