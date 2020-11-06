package com.sourceflag.framework.switchlogger.core.marker;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * AbstractSwitchLoggerMarker
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-31 14:22
 * @since 1.0
 */
public interface SwitchLoggerMarker extends Condition {

    default String getLoggerModel(ConditionContext context) {
        Environment environment = context.getEnvironment();
        return environment.getProperty("switch.logger.model");
    }

}
