package com.lakeside.data.redis;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import com.lakeside.core.utils.StringUtils;

/**
 * redis 数据操作类
 * 
 * redis作为Key-value型数据库，Redis提供了键（Key）和键值（Value）的映射关系。但是，除了常规的数值或字符串，Redis的键值还可以是以下形式之一：
 * 	Lists （列表），key对应的value是一个list对象
 * 	Sets （集合），key对应的value是一个Sets对象
 * 	Sorted sets （有序集合），key对应的value是一个sorted set对象
 * 	Hashes （哈希表），key对应的value是一个hash结构
 * 
 * 所有已h开头的方法，代表操作redis中的hash结构
 * 
 */
public class RedisDB {
	private String NameSpacPrefix= "";
	private JedisPool pool;
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param password
	 * @param nameSpacPrefix,该值是抽象出来的值，将作为命名前缀加到所有的key前面，最后的redis key将是 [nameSpacPrefix]:[key]
	 */
	public RedisDB(String host,int port, String password,String nameSpacPrefix){
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		pool = new JedisPool(poolConfig,host,port,Protocol.DEFAULT_TIMEOUT,password);
		if(StringUtils.isEmpty(nameSpacPrefix)){
			this.NameSpacPrefix = "";
		}else{
			this.NameSpacPrefix = nameSpacPrefix+":";
		}
	}
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param password
	 * @param poolConfig redis 参数设置
	 * @param nameSpacPrefix,该值是抽象出来的值，将作为命名前缀加到所有的key前面，最后的redis key将是 [nameSpacPrefix]:[key]
	 */
	public RedisDB(String host,int port, String password,JedisPoolConfig poolConfig,String nameSpacPrefix){
		pool = new JedisPool(poolConfig,host,port,Protocol.DEFAULT_TIMEOUT,password);
		if(StringUtils.isEmpty(nameSpacPrefix)){
			this.NameSpacPrefix = "";
		}else{
			this.NameSpacPrefix = nameSpacPrefix+":";
		}
	}
	
	public Map<String,String> hgetAll(String key){
		Jedis jedis=null;
		Map<String, String> maps;
		try {
			jedis = pool.getResource();
			maps = jedis.hgetAll(NameSpacPrefix+key);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
		return maps;
	}
	
	public String hget(final String key,final String field ){
		Jedis jedis=null;
		String value;
		try {
			jedis = pool.getResource();
			value = jedis.hget(NameSpacPrefix+key, field);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public Boolean hset(final String key,final String field,final String val){
		Jedis jedis =null;
		try {
			jedis = pool.getResource();
			jedis.hset(NameSpacPrefix+key,field, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
		return true;
	}
	
	public boolean hexists(final String key,final String field ){
		Jedis jedis=null;
		try {
			jedis = pool.getResource();
			boolean value = jedis.hexists(NameSpacPrefix+key, field);
			return value;
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
	}

	public long hdel(String key,final String field){
		Jedis jedis =null;
		try {
			jedis = pool.getResource();
			Long ret = jedis.hdel(NameSpacPrefix+key, field);
			return ret;
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
	}
	
	public void hincrease(String key,final String field){
		Jedis jedis =null;
		try {
			jedis = pool.getResource();
			jedis.hincrBy(NameSpacPrefix+key, field, 1);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
	}
	
	public void hdecrease(String key,final String field){
		Jedis jedis =null;
		try {
			jedis = pool.getResource();
			jedis.hincrBy(key, field, -1);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
	}
	
	public void decrease(String key){
		Jedis jedis =null;
		try {
			jedis = pool.getResource();
			jedis.decrBy(NameSpacPrefix+key, 1);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
	}
	
	public void increase(String key){
		Jedis jedis =null;
		try {
			jedis = pool.getResource();
			jedis.decrBy(NameSpacPrefix+key, -1);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			}
		}
	}

	
	public String get(String key){
		Jedis jedis=null;
		String value;
		try {
			jedis = pool.getResource();
			value = jedis.get(NameSpacPrefix+key);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public String set(String key,final String val){
		Jedis jedis=null;
		String value;
		try {
			jedis = pool.getResource();
			value = jedis.set(NameSpacPrefix+key,val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public boolean exists(String key){
		Jedis jedis=null;
		try {
			jedis = pool.getResource();
			boolean value = jedis.exists(NameSpacPrefix+key);
			return value;
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
	}
	
	public long del(String key){
		Jedis jedis=null;
		try {
			jedis = pool.getResource();
			long value = jedis.del(NameSpacPrefix+key);
			return value;
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
	}
	
	/**
	 * If the field already exists, 0 is returned, otherwise if a new
     * field is created 1 is returned.
     *  use to db lock
	 * @param key
	 * @param field
	 * @return
	 */
	public long hsetnx(String key,final String field){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.hsetnx(NameSpacPrefix+key,field,"1");
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	/**
	 * use to distribute lock with getset and del
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public long setnx(String key,final String val){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.setnx(NameSpacPrefix+key,val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public String getset(String key,final String val){
		Jedis jedis=null;
		String value;
		try {
			jedis = pool.getResource();
			value = jedis.getSet(NameSpacPrefix+key,val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public long rpush(String list,final String... val){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.rpush(NameSpacPrefix+list, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}

	public long rpushx(String list,final String val){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.rpushx(NameSpacPrefix+list, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}

	public String lpop(String list){
		Jedis jedis=null;
		String value;
		try {
			jedis = pool.getResource();
			value = jedis.lpop(NameSpacPrefix+list);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public long lpush(String list,final String... val){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.lpush(NameSpacPrefix+list, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}

	public long sadd(String set,final String... val){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.sadd(NameSpacPrefix+set, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}

	public long sremove(String set,final String... val){
		Jedis jedis=null;
		Long value;
		try {
			jedis = pool.getResource();
			value = jedis.srem(NameSpacPrefix+set, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
	
	public boolean sismember(String set,final String val){
		Jedis jedis=null;
		Boolean value;
		try {
			jedis = pool.getResource();
			value = jedis.sismember(NameSpacPrefix+set, val);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}

	public String rpop(String list){
		Jedis jedis=null;
		String value;
		try {
			jedis = pool.getResource();
			value = jedis.rpop(NameSpacPrefix+list);
		} finally {
			if(jedis!=null){
				pool.returnResource(jedis);
			} 
		}
		return value;
	}
}
