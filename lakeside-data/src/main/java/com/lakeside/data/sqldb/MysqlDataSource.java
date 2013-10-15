package com.lakeside.data.sqldb;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.lakeside.core.utils.StringUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MysqlDataSource {

	private DataSource dataSource = null;

	private NamedParameterJdbcTemplate jdbcTemplate;
	
	private DataSourceTransactionManager transactionManager;
	
	private String databaseName;
	
	private DefaultTransactionDefinition def;
	
	public String getDatabaseName() {
		return databaseName;
	}
	public NamedParameterJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public DataSourceTransactionManager getTransactionManager() {
		return transactionManager;
	}
	public DefaultTransactionDefinition getTransactionDefinition() {
		return def;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public MysqlDataSource(DataSource dataSource,NamedParameterJdbcTemplate jdbcTemplate){
		def = new DefaultTransactionDefinition();
		transactionManager = new DataSourceTransactionManager(dataSource);
	}
	
	public MysqlDataSource(String jdbcurl,String userName,String password){
		try {
			ComboPooledDataSource cdataSource = new ComboPooledDataSource();
			cdataSource.setDriverClass("com.mysql.jdbc.Driver");
			cdataSource.setJdbcUrl(jdbcurl);
			cdataSource.setUser(userName);
			cdataSource.setPassword(password);
			//<!-- these are C3P0 properties -->
			cdataSource.setAcquireIncrement(3);
			cdataSource.setMaxStatements(50);
			cdataSource.setMaxIdleTime(200);
			cdataSource.setIdleConnectionTestPeriod(60);
			cdataSource.setTestConnectionOnCheckout(true);
			this.dataSource = cdataSource;
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			def = new DefaultTransactionDefinition();
			transactionManager = new DataSourceTransactionManager(dataSource);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
	}
	
	public MysqlDataSource(String host,String port,String db,String userName,String password){
		this(StringUtils.format("jdbc:mysql://{0}:{1}/{2}?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8", host,port,db), userName, password);
		this.databaseName = db;
	}
}