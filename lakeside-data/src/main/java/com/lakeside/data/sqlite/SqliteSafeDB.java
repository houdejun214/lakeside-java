package com.lakeside.data.sqlite;

import java.io.File;
import java.io.IOException;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.lakeside.core.utils.FileUtils;

public class SqliteSafeDB extends SqliteDB {
	
	protected static Object syncObj = new Object();

	protected SQLiteQueue queue  = null;
	
	private boolean startTrans = false;
	
	public SqliteSafeDB(String dbpath) throws SQLiteException {
		FileUtils.insureFileDirectory(dbpath);
		queue = new SQLiteQueue(new File(dbpath));
		queue.start();
	}
	
	public void initConnection(SQLiteConnection connection) throws SQLiteException {
	  this.db = connection;
	}
	
	
	@Override
	public void close() throws IOException {
		try {
			queue.stop(true).join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void beginTransaction(){
		if(!startTrans){
			synchronized(syncObj){
				if(startTrans){
					return;
				}
				queue.execute(new SQLiteJob<Boolean>() {
				    protected Boolean job(SQLiteConnection connection) throws SQLiteException {
				    	connection.exec("BEGIN");
				    	return true;
				    }
				}).complete();
				startTrans = true;
			}
		}
	}
	
	public void commitTransaction(){
		if(startTrans){
			synchronized(syncObj){
				if(!startTrans){
					return;
				}
				queue.execute(new SQLiteJob<Boolean>() {
				    protected Boolean job(SQLiteConnection connection) throws SQLiteException {
				    	connection.exec("COMMIT");
				    	return true;
				    }
				}).complete();
				startTrans = false;
			}
		}
	}

	// flush current transaction and begin a new transaction
	protected void flushTransaction(){
		if(startTrans){
			synchronized(syncObj){
				queue.execute(new SQLiteJob<Boolean>() {
				    protected Boolean job(SQLiteConnection connection) throws SQLiteException {
				    	connection.exec("COMMIT");
				    	connection.exec("BEGIN");
				    	return true;
				    }
				}).complete();
			}
		}
	}
	
	protected void flushTransactionWithNew(){
		if(startTrans){
			synchronized(syncObj){
				if(!startTrans){
					return;
				}
				queue.execute(new SQLiteJob<Boolean>() {
				    protected Boolean job(SQLiteConnection connection) throws SQLiteException {
				    	connection.exec("COMMIT");
				    	connection.exec("BEGIN");
				    	return true;
				    }
				}).complete();
			}
		}else{
			synchronized(syncObj){
				if(startTrans){
					return;
				}
				queue.execute(new SQLiteJob<Boolean>() {
				    protected Boolean job(SQLiteConnection connection) throws SQLiteException {
				    	connection.exec("BEGIN");
				    	return true;
				    }
				}).complete();
				startTrans = true;
			}
		}
	} 
}
