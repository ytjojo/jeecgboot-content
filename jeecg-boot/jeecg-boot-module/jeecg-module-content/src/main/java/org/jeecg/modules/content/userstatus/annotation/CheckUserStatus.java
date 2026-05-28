package org.jeecg.modules.content.userstatus.annotation;

import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户状态检查注解。
 * 用于声明接口所需用户状态或禁止的用户状态。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckUserStatus {

    /**
     * 允许访问的用户状态列表。
     * 如果为空，则不限制允许的状态。
     */
    UserStatusEnum[] allow() default {};

    /**
     * 禁止的功能列表。
     * 如果用户状态限制了这些功能，则拒绝访问。
     */
    String[] forbid() default {};
}
