package com.lakeside.data.mongo;

import com.lakeside.core.utils.Assert;
import com.lakeside.core.utils.StringUtils;
import com.mongodb.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Arrays;
import java.util.WeakHashMap;

public class MongoDataSource {

    private Mongo m = null;
    private String host;
    private int port = 27017;
    private String defaultDBName;
    private static final Object sync = new Object();
    private Datastore defaultDatastore;
    private volatile WeakHashMap<String, Datastore> datastores = new WeakHashMap<>();
    private DB defaultDB;
    private Morphia ds;
    private String userName;
    private String password;

    /**
     * initialize the defaultDB
     */
    public void initDb() {
        synchronized (sync) {
            Assert.hasText(host);
            Assert.hasText(defaultDBName);
            if (m != null) {
                return;
            }
            try {
                if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
                    MongoCredential credential = MongoCredential.createMongoCRCredential(userName, defaultDBName, this.password.toCharArray());
                    m = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
                } else {
                    m = new MongoClient(host, port);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            ds = new Morphia();
            defaultDatastore = doGetDatastore(this.defaultDBName);
            defaultDB = defaultDatastore.getDB();
        }
    }

    /**
     * get dataStore of the db.
     * @param dbName
     * @return
     */
    private Datastore doGetDatastore(String dbName) {
        Datastore datastore = datastores.get(dbName);
        if (datastore == null) {
            synchronized (MongoDataSource.class) {
                datastore = datastores.get(dbName);
                if (datastore == null) {
                    datastore = ds.createDatastore(m, dbName);
                    if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
                        DB db = datastore.getDB();
                        if (db != null) {
                            db.authenticate(userName,this.password.toCharArray());
                        }
                    }
                    datastores.put(dbName, datastore);
                }
            }
        }
        return datastore;
    }

    public Mongo getM() {
        return m;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDefaultDBName() {
        return defaultDBName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDefaultDBName(String defaultDBName) {
        this.defaultDBName = defaultDBName;
    }

    public DB getDefaultDB() {
        return defaultDB;
    }

    public DB getDb(String dbName) {
        Datastore datastore = doGetDatastore(dbName);
        return datastore.getDB();
    }

    public Datastore getDefaultDatastore() {
        return defaultDatastore;
    }

    public Datastore getDatastore(String dbName) {
        Datastore datastore = doGetDatastore(dbName);
        return datastore;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}