package com.lakeside.data.redis;

import org.apache.commons.lang.StringUtils;

import com.lakeside.data.redis.RedisDB;

/**
 * @author zhufb
 *
 * Distributed locking using Redis SETNX and GETSET 
 * 
 *  Usage::
       DistributelLock dl = new DistributelLock(crawlDB,"my_lock",60,20);
       try{
         dl.lock();
         System.out.println("111");
         dl.release();
       }catch(DistributelLockTimeOutException e){
           
       }

    :param  key         lock key
    
    :param  expires     We consider any existing lock older than
                        ``expires`` seconds to be invalid in order to
                        detect crashed clients. This value must be higher
                        than it takes the critical section to execute.
    :param  timeout     If another client has already obtained the lock,
                        sleep for a maximum of ``timeout`` seconds before
                        giving up. A value of 0 means we never wait.

 *
 */
public class DistributeLock {
	
	private RedisDB db;
	private String key = "dstrb_lock";
	private int expires = 60;
	private int timeout = 30;
	private long thisLockExpire;// note lock expire time after lock successfully, which will be used to check when release this lock
	
	public DistributeLock(RedisDB db,String key,int expires,int timeout){
		this.db = db;
		this.key = key;
		this.expires = expires;
		this.timeout = timeout;
	}
	
	public DistributeLock(RedisDB db,String key){
		this.db = db;
		this.key = key;
	}

	public DistributeLock(RedisDB db){
		this.db = db;
	}
	
	/**
	 * distribute lock with redis if success return ,otherwise throws DistributelLockTimeOutException
	 * 
	 * @throws DistributelLockTimeOutException
	 */
	public void lock() throws DistributelLockTimeOutException{
		for(int _t = this.timeout;_t>=0;_t--){
			// get current time (s)
			long current_time = System.currentTimeMillis()/1000;
			// get this lock expires time (s)
			long _expires = current_time + expires + 1;
			String expires_str = String.valueOf(_expires);
			// get lock
			if(db.setnx(key, expires_str) == 1){
				thisLockExpire = _expires;
				return;
			}
			// lock failed then get redis lock expire time
			String db_value = db.get(key);
			// check dbexpire time ,if expired  get lock by getset 
			if(StringUtils.isEmpty(db_value)||Long.valueOf(db_value)<=current_time){
				//  if db value equals this lock expire value lock successfully, otherwise failed
				// get set is set current value ,return old value
				if(db_value.equals(db.getset(key, expires_str))){
					thisLockExpire = _expires;
					return;
				}
			}
			wait(1);
		} 
		throw new DistributelLockTimeOutException();
	}
	
	/**
	 * release redis lock
	 */
	public void release(){
		// check this lock for expire,if expired ,will not execute deleting operate
		long current_time = System.currentTimeMillis()/1000;
		if(current_time >= thisLockExpire){
			return;
		}
		this.db.del(key);
	}

	public void wait(int s){
		try {
			Thread.sleep(s*1000);
		} catch (InterruptedException e) {
			throw new RuntimeException("crawl lock waiting exception",e);
		}
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getKey() {
		return key;
	}

	public int getExpires() {
		return expires;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public void setExpires(int expires) {
		this.expires = expires;
	}

	public class DistributelLockTimeOutException extends Exception{
		private DistributelLockTimeOutException(){
			super(" Time out while waiting for distribute lock ...");
		}
	}
	
}
