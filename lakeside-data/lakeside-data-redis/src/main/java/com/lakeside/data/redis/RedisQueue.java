package com.lakeside.data.redis;


/**
 * Queue base on redis List Data Structure
 * Current only support to String object as the queue item. Other Object must to serialize to String first.
 * 
 * @author houdejun
 */
public class RedisQueue {
	
	private final RedisDB db;
	private final String listQueueKey;
	public RedisQueue(String host,int port, String password,String nameSpacPrefix){
		db = new RedisDB(host,port,password,nameSpacPrefix);
		this.listQueueKey = "queue";
	}
	
	public RedisQueue(String host,int port, String password,String nameSpacPrefix,String queueKey){
		db = new RedisDB(host,port,password,nameSpacPrefix);
		this.listQueueKey = queueKey;
	}
	
	/**
	 * Inserts the specified element into this queue 
	 * @param e
	 * @return
	 */
	public boolean add(String e) {
		try {
			db.lpush(listQueueKey, e.toString());
		} catch (Exception e1) {
			return false;
		}
		return true;
	}


    /**
     * Retrieves and removes the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
	public String poll() {
		String el = db.lpop(listQueueKey);
		return el;
	}

	/**
	 * check if the queue is empty
	 * @return
	 */
	public boolean isEmpty() {
		return db.llen(listQueueKey)<=0;
	}
}
