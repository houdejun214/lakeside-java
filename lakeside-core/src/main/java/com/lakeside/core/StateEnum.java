package com.lakeside.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;
import org.apache.commons.lang.enums.ValuedEnum;

/**
 * 
 * 状态枚举，用于存储系统中使用的状态.
 * 支持自动将状态值(整数）和枚举值建立一一对应关系.
 * 
 * @author qiumm
 *
 */
public abstract class StateEnum extends ValuedEnum {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5084408423658328277L;

	protected StateEnum(String name, int value) {
		super(name, value);
		this.initSimpleName2ClassMap();
	}
	
    public byte[] getByte(){
    	int intValue = this.getValue();
		return new byte[] {
	            (byte)(intValue >>> 24),
	            (byte)(intValue >>> 16),
	            (byte)(intValue >>> 8),
	            (byte)intValue};
    }
    
    private void initSimpleName2ClassMap(){
    	Class<? extends StateEnum> stateClass = this.getClass();
    	String stateSimpleName = stateClass.getSimpleName();
         synchronized( StateEnum.class ) {
        	 if(!simpleName2ClassMap.containsKey(stateSimpleName)){
        		 simpleName2ClassMap.put(stateSimpleName, stateClass);
        	 }
         }
    }
    
    /**
     * 存储简明与class的对应关系
     */
    private static Map<String,Class<? extends StateEnum>> simpleName2ClassMap = new HashMap<String,Class<? extends StateEnum>>();
    
    /**
     * 根据枚举对象的类型名称（类名的简写）和枚举值获取枚举名
     * @param simpleStateTypeName
     * @param value
     * @return
     */
    public static <T extends StateEnum> String getStateName(String simpleStateTypeName,int value){
    	if (simpleStateTypeName == null) {
			throw new IllegalArgumentException("The Enum Class must not be null");
		}
    	Class<? extends StateEnum> stateType = simpleName2ClassMap.get(simpleStateTypeName);
    	if (stateType == null) {
			throw new IllegalArgumentException("The Enum Class can't be found");
		}
		List<T> list = Enum.getEnumList(stateType);
		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			T enumeration = (T) it.next();
			if (enumeration.getValue() == value) {
				return enumeration.getName();
			}
		}
		return null;
    }
    
    /**
     * 根据枚举对象的类型和枚举值获取枚举名
     * @param stateType
     * @param value
     * @return
     */
    public static <T extends StateEnum> String getStateName(Class<T> stateType,int value){
    	if (stateType == null) {
			throw new IllegalArgumentException("The Enum Class must not be null");
		}
		List<T> list = Enum.getEnumList(stateType);
		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			T enumeration = (T) it.next();
			if (enumeration.getValue() == value) {
				return enumeration.getName();
			}
		}
		return null;
    }
    
    /**
     * 根据枚举对象的类型和枚举值获取枚举
     * @param stateType
     * @param value
     * @return
     */
	public static <T extends StateEnum> T getState(Class<T> stateType, int value) {
		if (stateType == null) {
			throw new IllegalArgumentException("The Enum Class must not be null");
		}
		List<T> list = Enum.getEnumList(stateType);
		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			T enumeration = (T) it.next();
			if (enumeration.getValue() == value) {
				return enumeration;
			}
		}
		return null;
	}
	
	/**
	 * 根据枚举对象的类型和枚举名称获取枚举
	 * @param stateType
	 * @param value
	 * @return
	 */
	public static <T extends StateEnum> T getState(Class<T> stateType, String name) {
		if (stateType == null) {
			throw new IllegalArgumentException("The Enum Class must not be null");
		}
		Enum e = Enum.getEnum(stateType,name);
		return (T) e;
	}
	
    public static <T extends StateEnum> T getState(Class<T> stateType,byte[] bytes){
    	int value = bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
		 return getState(stateType,value);
	}
   
}
