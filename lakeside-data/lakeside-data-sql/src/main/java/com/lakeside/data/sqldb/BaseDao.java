/**
 * Copyright (c) 2005-2009 springside.org.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 
 * $Id: SimpleHibernateDao.java 1139 2010-07-31 15:25:32Z calvinxiu $
 */
package com.lakeside.data.sqldb;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.lakeside.core.utils.Assert;
import com.lakeside.core.utils.ReflectionUtils;

/**
 * 封装Hibernate原生API的DAO泛型基类.
 * 
 * 可在Service层直接使用,也可以扩展泛型DAO子类使用.
 * 参考Spring2.5自带的Petlinc例子,取消了HibernateTemplate,直接使用Hibernate原生API.
 * 
 * @param <T> DAO操作的对象类型
 * @param <PK> 主键类型
 * 
 * @author calvin
 */
@SuppressWarnings("unchecked")
public class BaseDao<T, PK extends Serializable> {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected SessionFactory sessionFactory;

	protected Class<T> entityClass;
	
	protected NamedParameterJdbcTemplate jdbcTemplate;

	private DataSource dataSource;

	public static final HashMap<String,Object> EMPTY_PARAMETER = null;
	/**
	 * 用于Dao层子类使用的构造函数.
	 * 通过子类的泛型定义取得对象类型Class.
	 * eg.
	 * public class UserDao extends BaseDao<User, Long>
	 */
	public BaseDao() {
		this.entityClass = ReflectionUtils.getSuperClassGenricType(getClass());

	}

	/**
	 * 用于用于省略Dao层, 在Service层直接使用通用BaseDao的构造函数.
	 * 在构造函数中定义对象类型Class.
	 * eg.
	 * BaseDao<User, Long> userDao = new BaseDao<User, Long>(sessionFactory, User.class);
	 */
	public BaseDao(final SessionFactory sessionFactory, final Class<T> entityClass) {
		this.sessionFactory = sessionFactory;
		this.entityClass = entityClass;
	}

	/**
	 * 取得sessionFactory.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * 采用@Autowired按类型注入SessionFactory, 当有多个SesionFactory的时候Override本函数.
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 采用@Autowired按类型注入SessionFactory, 当有多个SesionFactory的时候Override本函数.
	 */
	@Autowired
	public void setSessionFactory(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Autowired
	public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 取得当前Session.
	 */
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * 保存新增或修改的对象.
	 */
	public void save(final T entity) {
		Assert.notNull(entity, "entity不能为空");
		getSession().saveOrUpdate(entity);
		logger.debug("save entity: {}", entity);
	}

	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	public void delete(final T entity) {
		Assert.notNull(entity, "entity不能为空");
		getSession().delete(entity);
		logger.debug("delete entity: {}", entity);
	}

	/**
	 * 按id删除对象.
	 */
	public void delete(final PK id) {
		Assert.notNull(id, "id不能为空");
		delete(get(id));
		logger.debug("delete entity {},id is {}", entityClass.getSimpleName(), id);
	}

	/**
	 * 初始化对象.
	 * 使用load()方法得到的仅是对象Proxy, 在传到View层前需要进行初始化.
	 * 只初始化entity的直接属性,但不会初始化延迟加载的关联集合和属性.
	 * 如需初始化关联属性,可实现新的函数,执行:
	 * Hibernate.initialize(user.getRoles())，初始化User的直接属性和关联集合.
	 * Hibernate.initialize(user.getDescription())，初始化User的直接属性和延迟加载的Description属性.
	 */
	public void initProxyProperty(Object proxyProperty) {
		Hibernate.initialize(proxyProperty);
	}

	/**
	 * Flush当前Session.
	 */
	public void flush() {
		getSession().flush();
	}
	/**
	 * 按id获取对象.
	 */
	public T get(final PK id) {
		Assert.notNull(id, "id不能为空");
		return (T) getSession().get(entityClass, id);
	}

	/**
	 *	获取全部对象.
	 */
	public List<T> getAll() {
		return find();
	}
	
	/**
	 * 合并对象
	 * @param entity
	 * @return
	 */
	public T merge(final T entity){
		Assert.notNull(entity, "entity不能为空");
		Session session = getSession();
		String idName = getIdName();
		PropertyDescriptor idp= BeanUtils.getPropertyDescriptor(entityClass, idName);
		PK idvalue=null;
		try {
			idvalue = (PK)idp.getReadMethod().invoke(entity);
		} catch (Exception e) {
			throw new FatalBeanException("Could not copy properties from source to target", e);
		} 
		T dest=null;
		if(idvalue!=null){
			dest=(T)session.get(entityClass, idvalue);
		}
		if(dest!=null){
			// merge the properties
			PropertyDescriptor[] descriptors =
	            BeanUtils.getPropertyDescriptors(entityClass);
			for (PropertyDescriptor p : descriptors) {
				if (p.getWriteMethod() != null) {
						try {
							Method readMethod = p.getReadMethod();
							if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
								readMethod.setAccessible(true);
							}
							Object value = readMethod.invoke(entity);
							if(value==null){
								continue;
							}
							Method writeMethod = p.getWriteMethod();
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(dest, value);
						}
						catch (Throwable ex) {
							throw new FatalBeanException("Could not copy properties from source to target", ex);
						}
				}
			}
		}else{
			// destination object is empty, save the entity object parameted
			dest=entity;
		}
		session.saveOrUpdate(dest);
		logger.debug("merge entity: {}", entity);
		return dest;
	}

	/**
	 * 按Criteria查询对象列表.
	 * 
	 * @param criterions 数量可变的Criterion.
	 */
	public List<T> find(final Criterion... criterions) {
		return createCriteria(criterions).list();
	}

	/**
	 * 根据Criterion条件创建Criteria.
	 * 
	 * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
	 * 
	 * @param criterions 数量可变的Criterion.
	 */
	protected Criteria createCriteria(final Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(entityClass);
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}

	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	public <X> List<X> find(final String hql, final Map<String, ?> values) {
		return createQuery(hql, values).list();
	}

	/**
	 * 按HQL查询唯一对象.
	 * 查询结果最多有一行，超过一行会抛出异常
	 * @param values 命名参数,按名称绑定.
	 */
	public <X> X findUnique(final String hql, final Map<String, ?> values) {
		return (X) createQuery(hql, values).uniqueResult();
	}

	/**
	 * 按属性查找唯一对象,匹配方式为相等.
	 * 查询结果最多有一行，超过一行会抛出异常
	 */
	public T findUniqueBy(final String propertyName, final Object value) {
		Assert.hasText(propertyName, "propertyName不能为空");
		Criterion criterion = Restrictions.eq(propertyName, value);
		return (T) createCriteria(criterion).uniqueResult();
	}

	/**
	 * 执行HQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	public int batchExecute(final String hql, final Map<String, ?> values) {
		Assert.hasText(hql, "hql不能为空");
		return createQuery(hql, values).executeUpdate();
	}

	/**
	 * 根据查询HQL与参数列表创建Query对象.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	public Query createQuery(final String hql, final Map<String, ?> values) {
		Assert.hasText(hql, "hql不能为空");
		Query query = getSession().createQuery(hql);
		if (values != null) {
			query.setProperties(values);
		}
		return query;
	}

	/**
	 * 取得对象的主键名.
	 */
	public String getIdName() {
		ClassMetadata meta = getSessionFactory().getClassMetadata(entityClass);
		return meta.getIdentifierPropertyName();
	}

	/**
	 * 基于jdbcTemplate查询count数或其他数字结果
	 * @param sql sql语句
	 * @param paramMap 命名参数,按名称绑定.
	 * @return 结果列表必须为1行，否则抛出异常
	 */
	public int jfindInt(final String sql, final Map<String, ?> paramMap) {
		return jdbcTemplate.queryForInt(sql, paramMap);
	}

	/**
	 * 基于jdbcTemplate列表查询，并进行对象封装
	 * @param <X>转换为的对象
	 * @param paramMap 命名参数,按名称绑定.
	 * @param elementType 要转换为的对象
	 * @return 查询结果列表，无对应结果时，返回空列表
	 */
	public <X> List<X> jfind(final String sql, final Map<String, ?> paramMap, final Class<X> elementType) {
		return jdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<X>(elementType));
	}

	/**
	 * 基于jdbcTemplate的列表查询
	 * @param paramMap 命名参数,按名称绑定.
	 * @return 查询结果列表，无对应结果时，返回空列表
	 */
	public List<Map<String, Object>> jfind(final String sql, final Map<String, ?> paramMap) {
		return jdbcTemplate.queryForList(sql, paramMap);
	}

	/**
	 * 基于jdbcTemplate查询
	 * @param paramMap 命名参数,按名称绑定.
	 * @return 返回第一个Map对象，没有时返回null
	 */
	public Map<String, Object> jfindUnique(final String sql, final Map<String, ?> paramMap) {
		List<Map<String, Object>> result = jfind(sql, paramMap);
		if (result == null || result.size() == 0) {
			return null;
		}
		return result.get(0);
	}

	/**
	 * 基于jdbcTemplate查询，并进行对象封装
	 * @param paramMap 参数列表
	 * @param requiredType 封装的对象类型
	 * @return
	 */
	public <X> X jfindUnique(final String sql, final Map<String, ?> paramMap, Class<X> requiredType) {
		List<X> result = jfind(sql, paramMap, requiredType);
		if (result == null || result.size() == 0) {
			return null;
		}
		return result.get(0);
	}
	
	public long insertWithGeneratedKey(final String sql, final Map<String, ?> paramMap){
		
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(paramMap);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int row = this.jdbcTemplate.update(sql, mapSqlParameterSource, keyHolder);
		if (row > 0)
             return keyHolder.getKey().longValue(); //line 72
		return -1;
	}
	
	public Transaction begainTransaction(){
		Session currentSession = this.sessionFactory.openSession();
		Transaction tran = currentSession.beginTransaction();
		tran.begin();
		return tran;
	}
	
	protected Map<String, Object> newParameters(){
		return new HashMap<String,Object>();
	}
}