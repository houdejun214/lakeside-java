package com.lakeside.data.sqldb;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.lakeside.core.utils.StringUtils;

public class MysqlDataSource {

	private DataSource dataSource = null;

	private NamedParameterJdbcTemplate jdbcTemplate;
	
	private String databaseName;
	
	public String getDatabaseName() {
		return databaseName;
	}
	public NamedParameterJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public MysqlDataSource(String jdbcurl,String userName,String password){
		DataSource cdataSource = new DataSource();
		cdataSource.setDriverClassName("com.mysql.jdbc.Driver");
		cdataSource.setUrl(jdbcurl);
		cdataSource.setUsername(userName);
		cdataSource.setPassword(password);
		cdataSource.setMaxActive(50);
		cdataSource.setMaxIdle(10);
		cdataSource.setMinIdle(0);
		/** 连接Idle10分钟后超时，每1分钟检查一次 **/
		cdataSource.setTimeBetweenEvictionRunsMillis(60000);
		cdataSource.setMinEvictableIdleTimeMillis(600000);
		/** (boolean) The default read-only state of connections created by this pool. 
		 * If not set then the setReadOnly method will not be called. (Some drivers don't support read only mode, ex: Informix) **/
		cdataSource.setDefaultReadOnly(true);
		/** The default auto-commit state of connections created by this pool. 
		 * If not set, default is JDBC driver default (If not set then the setAutoCommit method will not be called.) **/
		cdataSource.setDefaultAutoCommit(false);
		/*---------*/
		this.dataSource = cdataSource;
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}
	
	public MysqlDataSource(String host,String port,String db,String userName,String password){
		this(StringUtils.format("jdbc:mysql://{0}:{1}/{2}?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8", host,port,db), userName, password);
		this.databaseName = db;
	}
	

	/**
	 * close the data source
	 */
	public void close(){
		this.dataSource.close(true);
	}
}