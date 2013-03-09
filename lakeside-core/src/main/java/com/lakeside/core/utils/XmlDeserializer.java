/*
 * XmlDserializer.java        2010-11-9
 * Copyright (c) 2010 nycticebus team.
 * All rights reserved.
 * This project aims to provide a general,easy to use framework for developer.
 * It will reduce coding time for us as possible
 */
package com.lakeside.core.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Xml反序列化器。（实现为无需要Xml-Java绑定关系文件，直接根据对象进行Xml反序列化）
 * 此反序列化操作依据反序列对象进行。
 * 对象中的属性默认将按照XmlAttribute方式处理，如指定为XmlElement标记则系统将按照XmlElement方式处理。
 * 本序列化操作只能进行简单的Xml反序列化操作，不能满足一些复杂的应用场景 。
 * @author Dejun Hou
 */
@SuppressWarnings("rawtypes")
public class XmlDeserializer {

	private Class<?> objType = null;

	public XmlDeserializer(Class<?> type) {
		objType = type;
	}

	/**
	 * 读取Xml文档内容，并反序列化为一个Pojo对象
	 * @param sourceFile xml文件
	 * @return
	 */
	public Object read(File sourceFile) {
		try {
			Object newObj = objType.newInstance();
			SAXReader reader = new SAXReader();
			Document document = reader.read(sourceFile);
			read(document.getRootElement(), objType, newObj);
			return newObj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 读取一个节点的内容为对象的值
	 * @param currentNode 当前节点
	 * @param currentOjType 当前节点对应的Java对象类型
	 * @param obj 当前节点对应的Java对象
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private void read(Node currentNode, Class<?> currentOjType, Object obj) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<Field> properties = getProperties(currentOjType);
		Element currentEl = (Element) currentNode;
		for (Field f : properties) {
			Class<?> type = f.getType();
			if (isSimpleType(f)) {
				//简单类型将Xml中的值直接赋值给对象属性
				String val = getPropertyXmlValue(f, currentEl);
				if (!StringUtils.isEmpty(val)) {
					ReflectionUtils.invokeSetterMethod(obj, f.getName(), val);
				}
			} else if (type.isEnum()) {
				//枚举类型，需要先将Xml中的值转化为枚举类型
				String val = getPropertyXmlValue(f, currentEl);
				if (!StringUtils.isEmpty(val)) {
					Class<Enum> em = (Class<Enum>) f.getType();
					Object emVal = Enum.valueOf(em, val);
					ReflectionUtils.invokeSetterMethod(obj, f.getName(), emVal);
				}
			} else if (type.isArray()) {
				//数组类型,数组成员只能是作为Xml中的Elment方式来处理，迭代每个Element子元素进行分别读取
				Class elementType = type.getComponentType();
				String fieldName = getFieldXmlBindName(f);
				List arrayNodeList = currentEl.selectNodes(fieldName);
				Object[] elments = (Object[]) Array.newInstance(elementType, arrayNodeList.size());
				for (int i = 0; i < arrayNodeList.size(); i++) {
					Object elementObj = elementType.newInstance();
					read((Node) arrayNodeList.get(i), elementType, elementObj);
					elments[i] = elementObj;
				}
			} else if (Collection.class.isAssignableFrom(type)) {
				//集合类型，读取方式同上面的数组类型
				Collection list = newCollectionInstance(type);
				Class elementType = getFieldGenericType(f, 0);
				String fieldName = getFieldXmlBindName(f);
				List arrayNodeList = currentEl.selectNodes(fieldName);
				for (int i = 0; i < arrayNodeList.size(); i++) {
					Object elementObj = elementType.newInstance();
					read((Node) arrayNodeList.get(i), elementType, elementObj);
					list.add(elementObj);
				}
			}
		}
	}

	/**
	 * 获取对象所定义的属性（包含getter和setter方法的private域)
	 * @param srcObjType 反射类型
	 * @return
	 */
	private List<Field> getProperties(Class<?> srcObjType) {
		Field[] fields = srcObjType.getDeclaredFields();
		Method[] methods = srcObjType.getDeclaredMethods();
		ArrayList<Field> properties = new ArrayList<Field>();
		ArrayList<String> allMethodNames = new ArrayList<String>();
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				allMethodNames.add(methods[i].getName());
			}
		}
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				String fieldName = fields[i].getName();
				int modifier = fields[i].getModifiers();
				if (!Modifier.isPrivate(modifier) || Modifier.isStatic(modifier)) {
					continue;
				}
				String getterMethodName = "get" + org.apache.commons.lang.StringUtils.capitalize(fieldName);
				String setterMethodName = "set" + org.apache.commons.lang.StringUtils.capitalize(fieldName);
				if (allMethodNames.contains(getterMethodName) && allMethodNames.contains(setterMethodName)) {
					properties.add(fields[i]);
				}
			}
		}
		return properties;
	}

	/**
	 * 获取此成员对应Xml中的值，根据成员的标记可能对应Xml中的attribute或子节点
	 * @param f 成员描述对象
	 * @param currentEl 当前节点
	 * @return
	 */
	private String getPropertyXmlValue(Field f, Element currentEl) {
		XmlElement ele = f.getAnnotation(XmlElement.class);
		String fieldName = f.getName();
		if (ele != null) {
			if (!StringUtils.isEmpty(ele.name())) {
				fieldName = ele.name();
			}
			Element propertyEl = (Element) currentEl.selectSingleNode(fieldName);
			return propertyEl == null ? null : propertyEl.getText();
		}
		XmlAttribute atr = f.getAnnotation(XmlAttribute.class);
		if (atr != null && !StringUtils.isEmpty(atr.name())) {
			fieldName = atr.name();
		}
		return currentEl.attributeValue(fieldName);
	}

	/**
	 * 是否是简单类型（系统自带的类型，不包括自定义的类型和集合类型）
	 * @param type
	 */
	private Boolean isSimpleType(Field field) {
		Class<?> type = field.getType();
		if (Boolean.TYPE.equals(type) || String.class.equals(type) || Long.class.equals(type)
				|| long.class.equals(type) || Integer.class.equals(type) || int.class.equals(type)
				|| Enum.class.equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * 创建一个集合对象
	 * @param type 集合对象类型
	 * @return
	 */
	private Collection newCollectionInstance(Class type) {
		Collection list = null;
		if (Collection.class.isAssignableFrom(type)) {
			if (List.class.isAssignableFrom(type)) {
				list = new ArrayList();
			}
		}
		return list;
	}

	/** 
	* 通过反射,获得Field泛型参数的实际类型. 如: public Map<String, Buyer> names; 
	* 
	* @param Field field 字段 
	* @param int index 泛型参数所在索引,从0开始. 
	* @return 泛型参数的实际类型, 如果没有实现ParameterizedType接口，即不支持泛型，所以直接返回 
	*         <code>Object.class</code> 
	*/
	private Class getFieldGenericType(Field field, int index) {
		Type genericFieldType = field.getGenericType();
		if (genericFieldType instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) genericFieldType;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			if (index >= fieldArgTypes.length || index < 0) {
				throw new RuntimeException("你输入的索引" + (index < 0 ? "不能小于0" : "超出了参数的总数"));
			}
			return (Class) fieldArgTypes[index];
		}
		return Object.class;
	}

	/**
	 * 获取指定的Xml绑定名称
	 * @param f 属性对象
	 * @return
	 */
	private String getFieldXmlBindName(Field f) {
		if (f == null) {
			return "";
		}
		String fieldName = f.getName();
		XmlElement ele = f.getAnnotation(XmlElement.class);
		if (ele != null && !StringUtils.isEmpty(ele.name())) {
			return ele.name();
		}
		XmlAttribute atr = f.getAnnotation(XmlAttribute.class);
		if (atr != null && !StringUtils.isEmpty(atr.name())) {
			return atr.name();
		}
		return fieldName;
	}
}
