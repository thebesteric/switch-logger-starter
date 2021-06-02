package io.github.thebesteric.framework.switchlogger.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreParam {

    String value() default "";

}
