package com.lakeside.core.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class Util {
	/**
	 * 校验2个对象是否相同
	 * @param first 
	 * @param second
	 * @return 相同返回true，不同返回false
	 * @see eq(String first,String second)
	 * 
	 */
	public static boolean eq(final Object first, final Object second) {
		if (first == null || second == null) {
			return false;
		}
		if (first == second) {
			return true;
		}
		if (first.equals(second)) {
			return true;
		}
		if (first instanceof Number && second instanceof Number) {
			return first.toString().equals(second.toString());
		}
		if (first instanceof Collection<?> && second instanceof Collection<?>) {
			Collection<?> cA = (Collection<?>) first;
			Collection<?> cB = (Collection<?>) second;
			return eq(cA, cB);
		}
		if (first instanceof Map<?, ?> && second instanceof Map<?, ?>) {
			Map<?, ?> mA = (Map<?, ?>) first;
			Map<?, ?> mB = (Map<?, ?>) second;
			return eq(mA, mB);
		}
		if (first.getClass().isArray() && second.getClass().isArray()) {
			Object[] firstArray = (Object[]) first;
			Object[] secondArray = (Object[]) second;
			return eq(firstArray, secondArray);
		}
		return false;
	}

	/**
	 * 校验2个String是否相同
	 * @param first
	 * @param second
	 * @return 相同返回true，不同返回false
	 */
	public static boolean eq(final String first, final String second) {
		if (first == null || second == null) {
			return false;
		}
		return first.equals(second);
	}

	/**
	 * 校验2个Map是否相同，
	 * 校验时不比较类型，仅比较包含的值
	 * @param first
	 * @param second
	 * @return 相同返回true，不同返回false
	 */
	public static boolean eq(final Map<?, ?> first, final Map<?, ?> second) {
		if (first == null || second == null) {
			return false;
		}
		if (first == second) {
			return true;
		}
		if (first.size() != second.size()) {
			return false;
		}
		Iterator<?> entryIt = first.entrySet().iterator();
		while (entryIt.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) entryIt.next();
			if (entry.getValue() == null) {
				return false;
			}
			if (!first.containsKey(entry.getKey())) {
				return false;
			}
			if (!eq(entry.getValue(), second.get(entry.getKey()))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验2个Collection是否相同，
	 * 校验时不比较类型，仅比较包含的值
	 * @param first
	 * @param second
	 * @return 相同返回true，不同返回false
	 */
	public static boolean eq(final Collection<?> first, final Collection<?> second) {
		if (first == null || second == null) {
			return false;
		}
		if (first == second) {
			return true;
		}
		if (first.size() != second.size()) {
			return false;
		}
		Iterator<?> itFirst = first.iterator();
		Iterator<?> itSecond = second.iterator();
		while (itFirst.hasNext()) {
			if (!eq(itFirst.next(), itSecond.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验2个数组是否相同，
	 * 校验时不比较类型，仅比较包含的值
	 * @param first
	 * @param second
	 * @return 相同返回true，不同返回false
	 */
	public static boolean eq(final Object[] first, final Object[] second) {
		if (first == null || second == null) {
			return false;
		}
		if (first == second) {
			return true;
		}
		if (first.length != second.length) {
			return false;
		}
		int len = first.length;
		for (int i = 0; i < len; i++) {
			if (!eq(first[i], second[i])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 生成一个随机数组，随机数组大小是length,随机数组中最大值不超过max
	 * @param max
	 * @param length
	 * @return
	 */
	public static int[] random(int max, int length) {
		Random r = new Random();
		int temp1, temp2;
		int send[] = new int[max];
		for(int i = 0;i<max;i++){
			send[i] = i;
		}
		int len = send.length;
		int returnValue[] = new int[length];
		for (int i = 0; i < length; i++) {
			temp1 = Math.abs(r.nextInt()) % len;
			returnValue[i] = send[temp1];
			temp2 = send[temp1];
			send[temp1] = send[len - 1];
			send[len - 1] = temp2;
			len--;
		}
		return returnValue;
	}
}
