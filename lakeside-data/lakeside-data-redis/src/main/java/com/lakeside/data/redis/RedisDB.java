package com.lakeside.data.redis;

import java.util.Map;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
	private JedisTemplate template;
	private static int DefaultMaxIdle = 10;
	private static int DefaultMaxActive = 10;
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param password
	 * @param nameSpacPrefix,该值是抽象出来的值，将作为命名前缀加到所有的key前面，最后的redis key将是 [nameSpacPrefix]:[key]
	 */
	public RedisDB(String host,int port, String password,String nameSpacPrefix){
		this(host,port,password,JedisUtils.createPoolConfig(DefaultMaxIdle, DefaultMaxActive),nameSpacPrefix);
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
		JedisPool pool = new JedisPool(poolConfig,host,port,JedisUtils.DEFAULT_TIMEOUT,password);
		template = new JedisTemplate(pool);
		if(StringUtils.isEmpty(nameSpacPrefix)){
			this.NameSpacPrefix = "";
		}else{
			this.NameSpacPrefix = nameSpacPrefix.concat(":");
		}
	}

	public String getFullKey(String key){
		return NameSpacPrefix.concat(key);
	}

	//*****************String operation*********************
	
	public String get(String key){
		return template.get(getFullKey(key));
	}
	
	public void set(String key,final String val){
		template.set(getFullKey(key), val);
	}
	
	public boolean exists(String key){
		return template.exists(getFullKey(key));
	}
	
	public Boolean del(String key){
		return template.del(getFullKey(key));
	}
	
	public Boolean setnx(String key,final String val){
		return template.setnx(getFullKey(key), val);
	}
	
	public String getSet(String key,final String val){
		return template.getSet(getFullKey(key), val);
	}
	
	// ****************hash map operation ********************* 
	public Map<String,String> hgetAll(String key){
		return template.hgetAll(getFullKey(key));
	}
	
	public String hget(String key,final String field ){
		return template.hget(getFullKey(key), field);
	}
	
	public boolean hset(final String key,final String field,final String val){
		return template.hset(getFullKey(key), field, val);
	}
	
	public Boolean hexists(final String key,final String field ){
		return template.hexists(getFullKey(key), field);
	}

	public Boolean hdel(String key,final String field){
		return template.hdel(getFullKey(key), field);
	}
	
	//*****************Queue operation(depend List Structure)*********************
	public void rpush(String key,final String... val){
		template.rpush(getFullKey(key), val);
	}

	public void lpush(String key,final String... val){
		template.lpush(getFullKey(key), val);
	}

	public String lpop(String key){
		return template.lpop(getFullKey(key));
	}

	public String rpop(String key){
		return template.rpop(getFullKey(key));
	}

	//*****************Set operation *********************
	public void sadd(String key,final String... val){
		template.sadd(getFullKey(key), val);
	}

	public boolean sremove(String key,final String val){
		return template.sremove(getFullKey(key), val);
	}
	
	public boolean sismember(String key,final String val){
		return template.sismember(getFullKey(key), val);
	}

	public JedisTemplate getTemplate() {
		return template;
	}
}
