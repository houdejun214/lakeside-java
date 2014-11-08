package com.lakeside.data.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import java.util.Map;
import java.util.Set;

public class MongoDao {
	
	protected Mongo m = null;
	protected DB db = null;
	private boolean initilized = false;
	private static Object syncObject = new Object();
	
	public void initilize(Mongo m,DB db){
		if(!initilized){
			synchronized(syncObject){
				if(!initilized){
					this.m= m;
					this.db = db;
				}
			}
		}
	}
	
	public void initilize(String host,int port,String dbName) {
		if(!initilized){
			synchronized(syncObject){
				if(!initilized){
					try {
						m = new Mongo( host , port );
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage(),e);
					}
					db = m.getDB( dbName );
					//coll = db.getCollection(collectName);
					initilized = true;
				}
			}
		}
	}
	
	public DBCollection getDBCollection(String colName){
		return db.getCollection(colName);
	}

	protected Set<String> getCollectionNames(){
		return db.getCollectionNames();
	}
	
	public void insert(String colName,Map<String,Object> obj){
		DBCollection coll = db.getCollection(colName);
		BasicDBObject doc = new BasicDBObject();
		doc.putAll(obj);
		coll.insert(doc);
	}
}
