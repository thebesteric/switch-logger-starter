package com.sourceflag.framework.switchlogger.annotation;

import com.sourceflag.framework.switchlogger.configuration.marker.SwitchLoggerMarker;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SwitchLoggerMarker.class)
public @interface EnableSwitchLogger {
}
