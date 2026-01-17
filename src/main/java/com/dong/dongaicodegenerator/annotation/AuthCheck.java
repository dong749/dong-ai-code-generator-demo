package com.dong.dongaicodegenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 用户必须有某一个角色才可以调用某一个接口，这里默认是空
     *
     * @return
     */
    String mustRole() default "";
}
