package com.lakeside.data.mongodb;


public class MongoDbRestoreTest {

	public void test() {
		String input= "D:\\temp\\mongodump-test";
		String host= "172.18.109.20";
		String database= "tencentUserRelationNewRestore";
		
		MongoDbRestore db = new MongoDbRestore(input, host, database);
		db.start();
	}

}
