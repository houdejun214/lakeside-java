package com.lakeside.data.sqldb;

import com.lakeside.core.utils.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * manage the mysql database connections. 
 * 
 * @author houdejun
 *
 */
public class MysqlDataSource {
	private static final Logger log = LoggerFactory.getLogger(MysqlDataSource.class);
	private static final String REPLICATION_DRIVER = "com.mysql.jdbc.ReplicationDriver";
	private static final String SINGLETON_DRIVER = "com.mysql.jdbc.Driver";
	private static final Object sync = new Object();
	private DataSource dataSource = null;
	private NamedParameterJdbcTemplate jdbcTemplate;
	private String databaseName;
	private DefaultTransactionDefinition def=null;
	private DataSourceTransactionManager transactionManager=null;
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
		if(isReplicationConnection(jdbcurl)){
			cdataSource.setDriverClassName(REPLICATION_DRIVER);
			log.info("will create mysql connection with replication mode");
		}else{
			cdataSource.setDriverClassName(SINGLETON_DRIVER);
			log.info("will create mysql connection with standard mode");
		}
		cdataSource.setUrl(jdbcurl);
		cdataSource.setUsername(userName);
		cdataSource.setPassword(password);
		cdataSource.setMaxActive(50);
		cdataSource.setMaxIdle(10);
		cdataSource.setMinIdle(0);
		cdataSource.setInitialSize(1);
		cdataSource.setDefaultAutoCommit(true);
		/** 连接Idle10分钟后超时，每1分钟检查一次 **/
		cdataSource.setTimeBetweenEvictionRunsMillis(60000);
		cdataSource.setMinEvictableIdleTimeMillis(600000);
		/*---------*/
		this.dataSource = cdataSource;
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}
	
	/** (boolean) The default read-only state of connections created by this pool. 
	 * If not set then the setReadOnly method will not be called. (Some drivers don't support read only mode, ex: Informix) **/
    public void setDefaultReadOnly(Boolean defaultReadOnly) {
        this.dataSource.setDefaultReadOnly(defaultReadOnly);
    }
	
	/** The default auto-commit state of connections created by this pool. 
	 * If not set, default is JDBC driver default (If not set then the setAutoCommit method will not be called.) **/
    public void setDefaultAutoCommit(Boolean autocommit) {
        this.dataSource.setDefaultAutoCommit(autocommit);
    }
	
	protected boolean isReplicationConnection(String jdbcurl){
		String regex = "jdbc:mysql://([^/]+)/.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(jdbcurl);
		while(matcher.find()){
			String hosts = matcher.group(1);
			String[] split = hosts.trim().split(",");
			if(split.length>1){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param db
	 * @param userName
	 * @param password
	 * @Deprecated, please use the constructor "MysqlDataSource(String jdbcurl,String userName,String password)" instead.
	 */
	@Deprecated
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
	/**
	 * init and create resources need by transaction operations.
	 */
	private void initAndCreateTransactionResources(){
		synchronized(sync){
			if(transactionManager==null){
				def = new DefaultTransactionDefinition();
				transactionManager = new DataSourceTransactionManager(dataSource);
			}
		}
	}
	
	/**
	 * get transactionManager object, create new one if not exists.
	 * @return
	 */
	public DataSourceTransactionManager getTransactionManager() {
		if(transactionManager==null){
			initAndCreateTransactionResources();
		}
		return transactionManager;
	}
	
	/**
	 * get transactionDefinition object, create new one if not exists.
	 * @return
	 */
	public DefaultTransactionDefinition getTransactionDefinition() {
		if(def==null){
			initAndCreateTransactionResources();
		}
		return def;
	}
}