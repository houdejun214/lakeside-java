package com.lakeside.data.mongodb;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoDBConnectionManager {
	
	private static Map<String,Mongo> mongos = new HashMap<String,Mongo>();

	public static DB getConnection(String host,String database, String user,String pass) {
		return getConnection(host, 27017, database, user, pass);
	}
	
	public static DB getConnection(String host,int port,String database, String user,String pass) {
		try {
			Mongo mongo = mongos.get(host);
			if(mongo==null){
				mongo = new Mongo(host,port);
				mongos.put(host,mongo);
			}
			DB db = mongo.getDB(database);
			return db;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
