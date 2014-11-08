package com.lakeside.data.mongo;

import org.mongodb.morphia.dao.BasicDAO;

/**
 * base mongo dao
 */
public class MongoDao<T, K> extends BasicDAO<T, K> {

    protected final MongoDataSource dataSource;

    public MongoDao(MongoDataSource dataSource) {
        super(dataSource.getDefaultDatastore());
        this.dataSource = dataSource;
    }
}
