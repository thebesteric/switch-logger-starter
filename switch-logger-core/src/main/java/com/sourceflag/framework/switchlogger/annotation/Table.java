package com.sourceflag.framework.switchlogger.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {

    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";
}
