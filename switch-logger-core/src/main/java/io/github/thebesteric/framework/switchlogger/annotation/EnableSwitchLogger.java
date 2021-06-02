package io.github.thebesteric.framework.switchlogger.annotation;

import io.github.thebesteric.framework.switchlogger.configuration.marker.SwitchLoggerMarker;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SwitchLoggerMarker.class)
@Documented
public @interface EnableSwitchLogger {
}
