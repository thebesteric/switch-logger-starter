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

    String level() default Level.INFO;

    String[] extra() default "";

    final class Level {
        public static final String INFO = "INFO";
        public static final String DEBUG = "DEBUG";
    }

}
