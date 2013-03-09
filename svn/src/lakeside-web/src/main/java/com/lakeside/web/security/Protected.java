package com.lakeside.web.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
}
