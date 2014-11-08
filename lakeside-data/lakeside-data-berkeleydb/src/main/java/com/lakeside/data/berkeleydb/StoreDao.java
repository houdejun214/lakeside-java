package com.lakeside.data.berkeleydb;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class StoreDao<PK,T> implements Closeable {
	
	private static final Logger log = LoggerFactory.getLogger("StoreDao");
	
	public PrimaryIndex<PK,T> pIdx;
	
	private EntityStore store;

	private Environment environment;

	private TransactionConfig config;

    public  StoreDao(EntityStore store,Class<PK> pkClass,Class<T> entityClass)
        throws DatabaseException {
        // Primary key for SimpleEntityClass classes
        this.pIdx = store.getPrimaryIndex(pkClass,entityClass);
        this.store = store;
        this.environment = store.getEnvironment();
		config = new TransactionConfig();
    }
    
    public void save(T obj){
    	this.pIdx.putNoReturn(obj);
    }
    
    public void save(Transaction tx,T obj){
    	this.pIdx.putNoReturn(tx,obj);
    }
    
    public T get(PK key){
    	return pIdx.get(key);
    }
    public T get(Transaction tx,PK key,LockMode mode){
    	return pIdx.get(tx,key,mode);
    }
    
    public boolean delete(Transaction tx, PK key){
    	return pIdx.delete(tx,key);
    }
    
    public SecondaryIndex<String, PK, T> getSecondIndex(String field){
    	SecondaryIndex<String, PK, T> secondaryIndex = this.store.getSecondaryIndex(this.pIdx, String.class, field);
    	return secondaryIndex;
    }
    public <SK> SecondaryIndex<SK, PK, T> getSecondIndex(Class<SK> type,String field){
    	SecondaryIndex<SK, PK , T> secondaryIndex =this.store.getSecondaryIndex(this.pIdx,type, field);
    	return secondaryIndex;
    }
    
    public long getCount(){
    	return pIdx.count();
    }
    
    public Transaction begainTransaction(){
    	Transaction tx = this.environment.beginTransaction(null, null);
		return tx;
    }
    
    public long getNextSequenceId(String name){
    	Sequence sequence = this.store.getSequence(name);
    	long id = sequence.get(null, 1);
		return id;
    }
    
	public void close() throws IOException {
		try {
	        if (store != null) {
	        	store.close();
	        }
		} catch (DatabaseException dbe) {
			log.error(dbe.getMessage(),dbe);
		}
	}
}