package com.sourceflag.framework.switchlogger.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SwitchLogger {

    @AliasFor("tag")
    String value() default "";

    @AliasFor("value")
    String tag() default "";

    String[] extra() default "";

}
