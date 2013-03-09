package com.lakeside.data.berkeleydb;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.core.utils.FileUtils;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class BerkeleyDB implements Closeable {
	
	private static final Logger log = LoggerFactory.getLogger("BerkeleyDB");
	
	private Environment environment;

	public BerkeleyDB(String dbpath){
		FileUtils.mkDirectory(dbpath);
		this.initilize(dbpath);
	}
	
	public void initilize(String dbpath){
		environment = null;
		try {
		    EnvironmentConfig envConfig = new EnvironmentConfig();
		    envConfig.setAllowCreate(true);
		    envConfig.setTransactional(true);
		    envConfig.setLockTimeout(20,TimeUnit.SECONDS);
		    environment = new Environment(new File(dbpath), envConfig);
		    
		} catch (DatabaseException dbe) {
		    // Exception handling goes here
		} 
	}
	
	protected Database openDatabase(String name){
		 // open a database, create it if it is not exists;
	    DatabaseConfig dbConfig = new DatabaseConfig(); 
	    dbConfig.setAllowCreate(true);
	    dbConfig.setTransactional(true);
	    Database database = environment.openDatabase(null, name, dbConfig); 
	    return database;
	}
	
	protected <T> StoreDao<String,T> openStore(String name,Class<T> entityClass){
		StoreConfig storeConfig = new StoreConfig();
	    storeConfig.setTransactional(true);
	    storeConfig.setAllowCreate(true);
		EntityStore entityStore = new EntityStore(environment,  name, storeConfig);
		StoreDao<String,T> dao = new StoreDao<String,T>(entityStore,String.class,entityClass);
		return dao;
	}
	
	protected <PK,T> StoreDao<PK,T> openStore(String name,Class<PK> pkClass,Class<T> entityClass){
		// store config
		StoreConfig storeConfig = new StoreConfig();
	    storeConfig.setTransactional(true);
	    storeConfig.setAllowCreate(true);
		EntityStore entityStore = new EntityStore(environment,  name, storeConfig);
		StoreDao<PK,T> dao = new StoreDao<PK,T>(entityStore,pkClass,entityClass);
		return dao;
	}
	
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment myDbEnvironment) {
		this.environment = myDbEnvironment;
	}

	public void close() throws IOException {
		try {
	        if (environment != null) {
	            environment.close();
	        }
		} catch (DatabaseException dbe) {
			log.error(dbe.getMessage(),dbe);
		}
	}
}
