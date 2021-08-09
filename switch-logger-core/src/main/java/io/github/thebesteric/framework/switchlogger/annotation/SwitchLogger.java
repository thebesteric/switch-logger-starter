package io.github.thebesteric.framework.switchlogger.annotation;

import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SwitchLogger {

    boolean enable() default true;

    @AliasFor("tag")
    String value() default "";

    @AliasFor("value")
    String tag() default "";

    String level() default Level.INFO;

    String[] extra() default "";

    final class Level {
        public static final String INFO = InvokeLog.LEVEL_INFO;
        public static final String DEBUG = InvokeLog.LEVEL_DEBUG;
    }

}
