/**
 * Copyright (c) 2005-2009 springside.org.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 
 * $Id: HibernateDao.java 763 2009-12-27 18:36:21Z calvinxiu $
 */
package com.lakeside.data.sqldb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.classic.QueryTranslatorImpl;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.transform.ResultTransformer;
import org.springframework.util.Assert;

import com.lakeside.data.sqldb.XqlBuilder.MatchType;
import com.lakeside.core.utils.ReflectionUtils;

/**
 * 封装PageBaseDao扩展功能的BaseDao泛型基类.
 * 
 * 扩展功能包括分页查询,按属性过滤条件列表查询.
 * 可在Service层直接使用,也可以扩展泛型DAO子类使用,见两个构造函数的注释.
 * 
 * @param <T> DAO操作的对象类型
 * @param <PK> 主键类型
 * 
 * @author calvin
 */
public class PageBaseDao<T, PK extends Serializable> extends BaseDao<T, PK> {
	/**
	 * 用于Dao层子类使用的构造函数.
	 * 通过子类的泛型定义取得对象类型Class.
	 * eg.
	 * public class UserDao extends PageBaseDao<User, Long>{
	 * }
	 */
	public PageBaseDao() {
		super();
	}

	/**
	 * 用于省略Dao层, Service层直接使用通用HibernateDao的构造函数.
	 * 在构造函数中定义对象类型Class.
	 * eg.
	 * PageBaseDao<User, Long> userDao = new PageBaseDao<User, Long>(sessionFactory, User.class);
	 */
	public PageBaseDao(final SessionFactory sessionFactory, final Class<T> entityClass) {
		super(sessionFactory, entityClass);
	}

	//-- 分页查询函数 --//
	/**
	 * 分页获取全部对象.
	 */
	public Page<T> getAll(final Page<T> page) {
		return findPage(page);
	}

	/**
	 * 按HQL分页查询.
	 * 
	 * @param page 分页参数.
	 * @param hql hql语句.
	 * @param values 命名参数,按名称绑定.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	@SuppressWarnings("unchecked")
	public <X> Page<X> findPage(final Page<X> page, final String hql, final Map<String, ?> values) {
		Assert.notNull(page, "page不能为空");
		if (page.isAutoCount()) {
			long totalCount = countHqlResult(hql, values);
			page.setTotalCount(totalCount);
		}
		String orderSql = setOrder(hql, page);

		Query q = createQuery(orderSql, values);
		setPageParameter(q, page);

		List<X> result = q.list();
		page.setResult(result);
		return page;
	}

	/**
	 * 按HQL分页查询.
	 * 
	 * @param page 分页参数.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	public <X> Page<X> findPage(final Page<X> page, final XqlBuilder xql) {
		Assert.notNull(page, "page不能为空");
		return findPage(page, xql.getXql(), xql.getParam());
	}

	/**
	 * 按HQL分页查询.
	 * 
	 * @param page 分页参数.
	 * @param hql hql语句.
	 * @param values 命名参数,按名称绑定.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	@SuppressWarnings("unchecked")
	public <X> Page<X> findPage(final Page<X> page, final String sql, final Map<String, ?> values,
			final Class<X> elementType) {
		Assert.notNull(page, "page不能为空");
		if (page.isAutoCount()) {
			long totalCount = countSqlResult(sql, values);
			page.setTotalCount(totalCount);
		}
		String orderSql = setOrder(sql, page);

		String pageOrderSql = setPageParameter(orderSql, (Map<String, Object>) values, page);

		List<X> result = jfind(pageOrderSql, values, elementType);
		page.setResult(result);
		return page;
	}

	/**
	 * 按HQL分页查询.
	 * 
	 * @param page 分页参数.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	public <X> Page<X> findPage(final Page<X> page, final XqlBuilder xql, final Class<X> elementType) {
		Assert.notNull(page, "page不能为空");
		return findPage(page, xql.getXql(), xql.getParam(), elementType);
	}

	/**
	 * 按Criteria分页查询.
	 * 
	 * @param page 分页参数.
	 * @param criterions 数量可变的Criterion.
	 * 
	 * @return 分页查询结果.附带结果列表及所有查询时的参数.
	 */
	@SuppressWarnings("unchecked")
	public Page<T> findPage(final Page<T> page, final Criterion... criterions) {
		Assert.notNull(page, "page不能为空");

		Criteria c = createCriteria(criterions);

		if (page.isAutoCount()) {
			int totalCount = countCriteriaResult(c);
			page.setTotalCount(totalCount);
		}

		setPageParameter(c, page);
		List<T> result = c.list();
		page.setResult(result);
		return page;
	}

	protected <X> String setOrder(final String hql, final Page<X> page) {
		if (!page.isOrderBySetted()) {
			return hql;
		}
		String[] orderByArray = StringUtils.split(page.getOrderBy(), ',');
		String[] orderArray = StringUtils.split(page.getOrder(), ',');

		Assert.isTrue(orderByArray.length == orderArray.length, "分页多重排序参数中,排序字段与排序方向的个数不相等");
		StringBuilder orderSql = new StringBuilder(StringUtils.substringBefore(hql, "order by"));

		orderSql.append("order by ");
		for (int i = 0; i < orderByArray.length; i++) {
			orderSql.append(orderByArray[i]).append(" ").append(orderArray[i]).append(",");
		}
		return orderSql.substring(0, orderSql.length() - 1);
	}

	protected <X> String setPageParameter(final String sql, final Map<String, Object> values, final Page<X> page) {
		Dialect dialect = ((SessionFactoryImpl) sessionFactory).getDialect();
		String pageSql = dialect.getLimitString(sql, page.getFirst() - 1, page.getPageSize());
		boolean hasOffset = page.getFirst() - 1 > 0 || dialect.forceLimitUsage();
		if (!hasOffset) {
			pageSql = pageSql.replace("?", ":pageLimit");
			values.put("pageLimit", page.getPageSize());
			return pageSql;
		}
		if (dialect.bindLimitParametersInReverseOrder()) {
			pageSql = StringUtils.replaceOnce(pageSql, "?", ":pageLimit");
			pageSql = StringUtils.replaceOnce(pageSql, "?", ":pageOffset");
		} else {
			pageSql = StringUtils.replaceOnce(pageSql, "?", ":pageOffset");
			pageSql = StringUtils.replaceOnce(pageSql, "?", ":pageLimit");
		}
		values.put("pageOffset", page.getFirst() - 1);
		values.put("pageLimit", page.getPageSize());
		if (dialect.useMaxForLimit()) {
			values.put("pageLimit", page.getFirst() - 1 + page.getPageSize());
		}
		return pageSql;
	}

	/**
	 * 设置分页参数到Query对象,辅助函数.
	 */
	protected <X> Query setPageParameter(final Query q, final Page<X> page) {
		//hibernate的firstResult的序号从0开始
		q.setFirstResult(page.getFirst() - 1);
		q.setMaxResults(page.getPageSize());
		return q;
	}

	/**
	 * 设置分页参数到Criteria对象,辅助函数.
	 */
	protected Criteria setPageParameter(final Criteria c, final Page<T> page) {
		//hibernate的firstResult的序号从0开始
		c.setFirstResult(page.getFirst() - 1);
		c.setMaxResults(page.getPageSize());

		if (page.isOrderBySetted()) {
			String[] orderByArray = StringUtils.split(page.getOrderBy(), ',');
			String[] orderArray = StringUtils.split(page.getOrder(), ',');

			Assert.isTrue(orderByArray.length == orderArray.length, "分页多重排序参数中,排序字段与排序方向的个数不相等");

			for (int i = 0; i < orderByArray.length; i++) {
				if (Page.ASC.equals(orderArray[i])) {
					c.addOrder(Order.asc(orderByArray[i]));
				} else {
					c.addOrder(Order.desc(orderByArray[i]));
				}
			}
		}
		return c;
	}

	/**
	 * 执行count查询获得本次Hql查询所能获得的对象总数.
	 * 
	 * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
	 */
	protected int countHqlResult(final String hql, final Map<String, ?> values) {

		String countHql = getCountSql(hql);

		try {
			int count = jfindInt(countHql, values);
			return count;
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
		}
	}

	/**
	 * 执行count查询获得本次Hql查询所能获得的对象总数.
	 * 
	 * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
	 */
	protected int countSqlResult(final String sql, final Map<String, ?> values) {

		String countSql = "select count(*) from (" + sql + ") totalcount";

		try {
			int count = jfindInt(countSql, values);
			return count;
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countSql, e);
		}
	}

	protected String getCountSql(String originalHql) {
		QueryTranslatorImpl queryTranslator = new QueryTranslatorImpl(originalHql, originalHql, Collections.EMPTY_MAP,
				(SessionFactoryImplementor) sessionFactory);

		queryTranslator.compile(Collections.EMPTY_MAP, false);
		String fromHql = StringUtils.substringBeforeLast(queryTranslator.getSQLString(), "order by");
		return "select count(*) from (" + fromHql + ") totalcount";
	}

	/**
	 * 执行count查询获得本次Criteria查询所能获得的对象总数.
	 */
	@SuppressWarnings("unchecked")
	protected int countCriteriaResult(final Criteria c) {
		CriteriaImpl impl = (CriteriaImpl) c;

		// 先把Projection、OrderBy取出来,清空后再执行Count操作
		Projection projection = impl.getProjection();
		ResultTransformer transformer = impl.getResultTransformer();

		List<CriteriaImpl.OrderEntry> orderEntries = null;
		try {
			orderEntries = (List<CriteriaImpl.OrderEntry>) ReflectionUtils.getFieldValue(impl, "orderEntries");
			ReflectionUtils.setFieldValue(impl, "orderEntries", new ArrayList<Object>());
		} catch (Exception e) {
			logger.error("不可能抛出的异常:{}", e.getMessage());
		}

		// 执行Count查询
		int totalCount = (Integer) c.setProjection(Projections.rowCount()).uniqueResult();

		// 将之前的Projection,ResultTransformer和OrderBy条件重新设回去
		c.setProjection(projection);

		if (projection == null) {
			c.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
		}
		if (transformer != null) {
			c.setResultTransformer(transformer);
		}
		try {
			ReflectionUtils.setFieldValue(impl, "orderEntries", orderEntries);
		} catch (Exception e) {
			logger.error("不可能抛出的异常:{}", e.getMessage());
		}

		return totalCount;
	}

	//-- 属性过滤条件(PropertyFilter)查询函数 --//

	/**
	 * 按属性查找对象列表,支持多种匹配方式.
	 * 
	 * @param matchType 匹配方式,目前支持的取值见PropertyFilter的MatcheType enum.
	 */
	public List<T> findBy(final String propertyName, final Object value, final MatchType matchType) {
		Criterion criterion = buildPropertyFilterCriterion(propertyName, value, matchType);
		return find(criterion);
	}

	/**
	 * 按属性条件参数创建Criterion,辅助函数.
	 */
	protected Criterion buildPropertyFilterCriterion(final String propertyName, final Object propertyValue,
			final MatchType matchType) {
		Assert.hasText(propertyName, "propertyName不能为空");
		Criterion criterion = null;
		try {

			//根据MatchType构造criterion
			if (MatchType.EQ.equals(matchType)) {
				criterion = Restrictions.eq(propertyName, propertyValue);
			} else if (MatchType.LIKE.equals(matchType)) {
				criterion = Restrictions.like(propertyName, (String) propertyValue, MatchMode.ANYWHERE);
			} else if (MatchType.LE.equals(matchType)) {
				criterion = Restrictions.le(propertyName, propertyValue);
			} else if (MatchType.LT.equals(matchType)) {
				criterion = Restrictions.lt(propertyName, propertyValue);
			} else if (MatchType.GE.equals(matchType)) {
				criterion = Restrictions.ge(propertyName, propertyValue);
			} else if (MatchType.GT.equals(matchType)) {
				criterion = Restrictions.gt(propertyName, propertyValue);
			}
		} catch (Exception e) {
			throw ReflectionUtils.convertReflectionExceptionToUnchecked(e);
		}
		return criterion;
	}

	/**
	 * 判断对象的属性值在数据库内是否唯一.
	 * 
	 * 在修改对象的情景下,如果属性新修改的值(value)等于属性原来的值(orgValue)则不作比较.
	 */
	public boolean isPropertyUnique(final String propertyName, final Object newValue, final Object oldValue) {
		if (newValue == null || newValue.equals(oldValue)) {
			return true;
		}
		Object object = findUniqueBy(propertyName, newValue);
		return (object == null);
	}
}
