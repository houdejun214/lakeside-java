/*
 * CommonDataType.java        2010-11-7
 * Copyright (c) 2010 nycticebus team.
 * All rights reserved.
 * This project aims to provide a general,easy to use framework for developer.
 * It will reduce coding time for us as possible
 */
package com.lakeside.core;

/**
 * 常用数据类型
 * @author Dejun Hou
 */
public enum DataType {
	Text, //文本
	HomePhone, //家庭电话
	MobilePhone, //移动电话
	Email, //邮箱地址
	IdCard, //身份证号
	PostCode, //邮政编码
	Number, //数字
	Decimal, //小数
	Percent, //百分数
	Int, //整数
	Year, //年
	YearMonth, //月
	Date, //日期 即年月日
	DateTime, //日期时间 即年月日时分秒
	Time, //时间  即时分秒
	Cn, //中文
	En, //英文
	Digit, //数字
	Currency, //货币金额
}
