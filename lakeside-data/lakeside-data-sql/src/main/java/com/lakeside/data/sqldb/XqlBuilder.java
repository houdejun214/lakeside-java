package com.lakeside.data.sqldb;

import com.lakeside.core.utils.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class XqlBuilder {
	/** 属性比较类型. */
	public enum ConnectType {
		AND, OR, NOT;
	}

	/** 属性比较类型. */
	public enum MatchType {
		EQ, LIKE, LT, GT, LE, GE, IN, NIN, NE;
	}

	private static final Map<MatchType, String> MatchMap = new HashMap<MatchType, String>();

	static {
		MatchMap.put(MatchType.EQ, "=");
		MatchMap.put(MatchType.NE, "<>");
		MatchMap.put(MatchType.LIKE, "like");
		MatchMap.put(MatchType.LT, "<");
		MatchMap.put(MatchType.LE, "<=");
		MatchMap.put(MatchType.GT, ">");
		MatchMap.put(MatchType.GE, ">=");
		MatchMap.put(MatchType.IN, "in");
		MatchMap.put(MatchType.NIN, "not in");
	}

	private static final Map<ConnectType, String> ConnectMap = new HashMap<ConnectType, String>();
	static {

		ConnectMap.put(ConnectType.AND, "and");
		ConnectMap.put(ConnectType.OR, "or");
		ConnectMap.put(ConnectType.NOT, "not");

	}
	public static final String BETWEENBEGIN = "bb";
	public static final String BETWEENEND = "be";

	public static XqlBuilder instance() {
		return new XqlBuilder();
	}

	public static XqlBuilder instance(String str) {
		return new XqlBuilder(str);
	}

	private final StringBuilder xql = new StringBuilder();

	private final Map<String, Object> map = new HashMap<String, Object>();

	public XqlBuilder() {

	}

	public XqlBuilder(String str) {
		xql.append(str);
	}

	private XqlBuilder addOr(ConnectType addor) {
		xql.append(getConnectSign(addor));
		return this;
	}

	public XqlBuilder and() {
		return addOr(ConnectType.AND);
	}

	public XqlBuilder or() {
		return addOr(ConnectType.OR);
	}

	public XqlBuilder append(String str) {
		xql.append(str);
		return this;
	}

	public XqlBuilder between(ConnectType ct, String tableAlias, String columnName, Object begin, Object end) {
		if (begin == null || end == null) {
			return this;
		}
		addOr(ct);
		String columnNameBegin = columnName + BETWEENBEGIN;
		String columnNameEnd = columnName + BETWEENEND;
		if (!StringUtils.isEmpty(tableAlias)) {
			xql.append(tableAlias).append(".");
		}
		xql.append(columnName).append(" between :").append(columnNameBegin).append(" and :").append(columnNameEnd);
		map.put(columnNameBegin, begin);
		map.put(columnNameEnd, end);
		return this;
	}

	public XqlBuilder between(String columnName, Object begin, Object end) {
		return between(ConnectType.AND, null, columnName, begin, end);
	}

	public XqlBuilder between(String tableAlias, String columnName, Object begin, Object end) {
		return between(ConnectType.AND, tableAlias, columnName, begin, end);
	}

	public XqlBuilder orbetween(String columnName, Object begin, Object end) {
		return between(ConnectType.OR, null, columnName, begin, end);
	}

	public XqlBuilder orbetween(String tableAlias, String columnName, Object begin, Object end) {
		return between(ConnectType.OR, tableAlias, columnName, begin, end);
	}

	public XqlBuilder condition(ConnectType andor, String tableAlias, MatchType matchType, String columnName,
			Object param) {
		if (param == null) {
			return this;
		}
		if(param instanceof String && param.equals("")){
			return this;
		}
		addOr(andor);
		conditionOnly(tableAlias, matchType, columnName, param);
		return this;
	}

	public XqlBuilder condition(String tableAlias, MatchType matchType, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, matchType, columnName, param);
	}

	public XqlBuilder condition(MatchType matchType, String columnName, Object param) {
		return condition(ConnectType.AND, null, matchType, columnName, param);
	}

	private void conditionOnly(String tableAlias, MatchType matchType, String columnName, Object param) {
		if (!StringUtils.isEmpty(tableAlias)) {
			xql.append(tableAlias).append(".");
		}
		xql.append(columnName).append(getMatchSign(matchType)).append(":").append(columnName);
		map.put(columnName, param);
	}

	public XqlBuilder eq(String columnName, Object param) {
		return condition(ConnectType.AND, null, MatchType.EQ, columnName, param);
	}

	public XqlBuilder eq(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, MatchType.EQ, columnName, param);
	}

	public XqlBuilder ge(String columnName, Object param) {
		return condition(ConnectType.AND, null, MatchType.GE, columnName, param);
	}

	public XqlBuilder ge(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, MatchType.GE, columnName, param);
	}

	public XqlBuilder gt(String columnName, Object param) {
		return condition(ConnectType.AND, null, MatchType.GT, columnName, param);
	}

	public XqlBuilder gt(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, MatchType.GT, columnName, param);
	}

	public XqlBuilder le(String columnName, Object param) {
		return condition(ConnectType.AND, null, MatchType.LE, columnName, param);
	}

	public XqlBuilder le(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, MatchType.LE, columnName, param);
	}

	public XqlBuilder lt(String columnName, Object param) {
		return condition(ConnectType.AND, null, MatchType.LT, columnName, param);
	}

	public XqlBuilder lt(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, MatchType.LT, columnName, param);
	}

	public XqlBuilder ne(String columnName, Object param) {
		return condition(ConnectType.AND, null, MatchType.NE, columnName, param);
	}

	public XqlBuilder ne(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.AND, tableAlias, MatchType.NE, columnName, param);
	}

	public XqlBuilder oreq(String columnName, Object param) {
		return condition(ConnectType.OR, null, MatchType.EQ, columnName, param);
	}

	public XqlBuilder oreq(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.OR, tableAlias, MatchType.EQ, columnName, param);
	}

	public XqlBuilder orge(String columnName, Object param) {
		return condition(ConnectType.OR, null, MatchType.GE, columnName, param);
	}

	public XqlBuilder orge(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.OR, tableAlias, MatchType.GE, columnName, param);
	}

	public XqlBuilder orgt(String columnName, Object param) {
		return condition(ConnectType.OR, null, MatchType.GT, columnName, param);
	}

	public XqlBuilder orgt(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.OR, tableAlias, MatchType.GT, columnName, param);
	}

	public XqlBuilder orle(String columnName, Object param) {
		return condition(ConnectType.OR, null, MatchType.LE, columnName, param);
	}

	public XqlBuilder orle(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.OR, tableAlias, MatchType.LE, columnName, param);
	}

	public XqlBuilder orlt(String columnName, Object param) {
		return condition(ConnectType.OR, null, MatchType.LT, columnName, param);
	}

	public XqlBuilder orlt(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.OR, tableAlias, MatchType.LT, columnName, param);
	}

	public XqlBuilder orne(String columnName, Object param) {
		return condition(ConnectType.OR, null, MatchType.NE, columnName, param);
	}

	public XqlBuilder orne(String tableAlias, String columnName, Object param) {
		return condition(ConnectType.OR, tableAlias, MatchType.NE, columnName, param);
	}

	public XqlBuilder like(ConnectType andor, String tableAlias, MatchType matchType, String columnName, String param) {
		if (param == null) {
			return this;
		}
		if(param instanceof String && param.equals("")){
			return this;
		}
		addOr(andor);
		String paramLike = "%" + param + "%";
		conditionOnly(tableAlias, matchType, columnName, paramLike);
		return this;
	}

	public XqlBuilder like(String columnName, String param) {
		return like(ConnectType.AND, null, MatchType.LIKE, columnName, param);
	}

	public XqlBuilder like(String tableAlias, String columnName, String param) {
		return like(ConnectType.AND, tableAlias, MatchType.LIKE, columnName, param);
	}

	public XqlBuilder orlike(String columnName, String param) {
		return like(ConnectType.OR, null, MatchType.LIKE, columnName, param);
	}

	public XqlBuilder orlike(String tableAlias, String columnName, String param) {
		return like(ConnectType.OR, tableAlias, MatchType.LIKE, columnName, param);
	}

	public XqlBuilder conditionForIn(ConnectType andor, String tableAlias, MatchType matchType, String columnName,
			Object[] param) {
		if (param == null || param.length == 0) {
			return this;
		}
		addOr(andor);
		conditionForInOnly(tableAlias, matchType, columnName, param);
		return this;
	}

	public XqlBuilder conditionForIn(ConnectType andor, String tableAlias, MatchType matchType, String columnName,
			Object param) {
		if (param == null) {
			return this;
		}
		addOr(andor);
		conditionForInOnly(tableAlias, matchType, columnName, param);
		return this;
	}

	private void conditionForInOnly(String tableAlias, MatchType matchType, String columnName, Object[] param) {
		conditionForInOnly(tableAlias, matchType, columnName, Arrays.asList(param));
	}

	private void conditionForInOnly(String tableAlias, MatchType matchType, String columnName, Object param) {
		if (!StringUtils.isEmpty(tableAlias)) {
			xql.append(tableAlias).append(".");
		}
		xql.append(columnName).append(getMatchSign(matchType)).append("(:").append(columnName).append(")");
		map.put(columnName, param);
	}

	public XqlBuilder in(String columnName, Object[] param) {
		return conditionForIn(ConnectType.AND, null, MatchType.IN, columnName, param);
	}

	public XqlBuilder in(String columnName, Object param) {
		return conditionForIn(ConnectType.AND, null, MatchType.IN, columnName, param);
	}

	public XqlBuilder in(String tableAlias, String columnName, Object[] param) {
		return conditionForIn(ConnectType.AND, tableAlias, MatchType.IN, columnName, param);
	}

	public XqlBuilder in(String tableAlias, String columnName, Object param) {
		return conditionForIn(ConnectType.AND, tableAlias, MatchType.IN, columnName, param);
	}

	public XqlBuilder nin(String columnName, Object[] param) {
		return conditionForIn(ConnectType.AND, null, MatchType.NIN, columnName, param);
	}

	public XqlBuilder nin(String columnName, Object param) {
		return conditionForIn(ConnectType.AND, null, MatchType.NIN, columnName, param);
	}

	public XqlBuilder nin(String tableAlias, String columnName, Object[] param) {
		return conditionForIn(ConnectType.AND, tableAlias, MatchType.NIN, columnName, param);
	}

	public XqlBuilder nin(String tableAlias, String columnName, Object param) {
		return conditionForIn(ConnectType.AND, tableAlias, MatchType.NIN, columnName, param);
	}

	public XqlBuilder orin(String columnName, Object[] param) {
		return conditionForIn(ConnectType.OR, null, MatchType.IN, columnName, param);
	}

	public XqlBuilder orin(String columnName, Object param) {
		return conditionForIn(ConnectType.OR, null, MatchType.IN, columnName, param);
	}

	public XqlBuilder orin(String tableAlias, String columnName, Object[] param) {
		return conditionForIn(ConnectType.OR, tableAlias, MatchType.IN, columnName, param);
	}

	public XqlBuilder orin(String tableAlias, String columnName, Object param) {
		return conditionForIn(ConnectType.OR, tableAlias, MatchType.IN, columnName, param);
	}

	public XqlBuilder ornin(String columnName, Object[] param) {
		return conditionForIn(ConnectType.OR, null, MatchType.NIN, columnName, param);
	}

	public XqlBuilder ornin(String columnName, Object param) {
		return conditionForIn(ConnectType.OR, null, MatchType.NIN, columnName, param);
	}

	public XqlBuilder ornin(String tableAlias, String columnName, Object[] param) {
		return conditionForIn(ConnectType.OR, tableAlias, MatchType.NIN, columnName, param);
	}

	public XqlBuilder ornin(String tableAlias, String columnName, Object param) {
		return conditionForIn(ConnectType.OR, tableAlias, MatchType.NIN, columnName, param);
	}

	public XqlBuilder put(String columnName, Object param) {
		map.put(columnName, param);
		return this;
	}

	public String getConnectSign(ConnectType connectType) {
		return " " + ConnectMap.get(connectType) + " ";
	}

	public String getMatchSign(MatchType matchType) {
		return " " + MatchMap.get(matchType) + " ";
	}

	public Map<String, ?> getParam() {
		return map;
	}

	public String getXql() {
		return xql.toString();
	}
}
