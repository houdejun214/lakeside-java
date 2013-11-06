package com.lakeside.data.mongodb;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * 
 * Mongodb 数据源描述累
 * 
 * @author qiumm
 *
 */
public class MongoDataSource {
	
	private Mongo m = null;
	private DB db = null;
	private String host;
	private int port = 27017;
	private String dbName;
	private static final Object sync = new Object();

	private void initDb() {
		synchronized(sync){
			if(m!=null || db!=null){
				return;
			}
			try {
				m = new Mongo( host , port );
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
			db = m.getDB( dbName );
		}
	}
	
	public DB getMongoDb(){
		if(db==null){
			initDb();
		}
		return this.db;
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

	public String getDbName() {
		return dbName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
}
