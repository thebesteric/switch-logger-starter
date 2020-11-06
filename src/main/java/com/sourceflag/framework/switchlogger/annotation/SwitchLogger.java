package com.sourceflag.framework.switchlogger.annotation;

import org.springframework.core.annotation.AliasFor;

public @interface SwitchLogger {

    @AliasFor("extra")
    String value() default "";

    @AliasFor("value")
    String extra() default "";

    boolean exclude() default false;

}
