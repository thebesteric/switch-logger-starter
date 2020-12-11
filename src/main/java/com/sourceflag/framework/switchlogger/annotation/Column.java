package com.sourceflag.framework.switchlogger.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name() default "";

    String type() default "varchar";

    int length() default 255;

}
