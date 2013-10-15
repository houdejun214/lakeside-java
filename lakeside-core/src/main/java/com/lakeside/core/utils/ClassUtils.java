package com.lakeside.core.utils;

import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;

public class ClassUtils {
	
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ClassUtils.class.getClassLoader();
		}
		return cl;
	}
	
	/**
	 * Determine whether the given class has a method with the given signature.
	 * <p>Essentially translates <code>NoSuchMethodException</code> to "false".
	 * @param clazz	the clazz to analyze
	 * @param methodName the name of the method
	 * @param paramTypes the parameter types of the method
	 * @return whether the class has a corresponding method
	 * @see java.lang.Class#getMethod
	 */
	public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
	}

	/**
	 * Determine whether the given class has a method with the given signature,
	 * and return it if available (else return <code>null</code>).
	 * <p>Essentially translates <code>NoSuchMethodException</code> to <code>null</code>.
	 * @param clazz	the clazz to analyze
	 * @param methodName the name of the method
	 * @param paramTypes the parameter types of the method
	 * @return the method, or <code>null</code> if not found
	 * @see java.lang.Class#getMethod
	 */
	public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		try {
			return clazz.getMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			return null;
		}
	}
	
	public static <T> Set<Class<? extends T>> getSubClass(Class<T> parentClass,String basePath){
		Reflections reflections = new Reflections(basePath);
	     Set<Class<? extends T>> subTypes = 
	               reflections.getSubTypesOf(parentClass);
	     return subTypes;
	}
}
