package com.lakeside.web.security;

import java.lang.annotation.*;

/**
 * 标记此类或此方法需要用户认证，只有登录用户才能进行访问。
 * 
 * @author houdejun
 *
 */

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Protected {
	
	/**
	 * define the require user role
	 * @return
	 */
	String[] role() default "";
	
	/**
	 * define the require role permission
	 * @return
	 */
	String[] permission() default "";
	
	/**
	 * define if can access anonymity
	 * @return
	 */
	boolean allowAnon() default false;
}
