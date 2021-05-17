package com.sourceflag.framework.switchlogger.configuration.marker;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

/**
 * SwitchLoggerConditionMarker
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-31 14:22
 * @since 1.0
 */
public interface SwitchLoggerConditionMarker extends Condition {

    default String getLoggerModel(ConditionContext context) {
        Environment environment = context.getEnvironment();
        return environment.getProperty(SwitchLoggerProperties.PROPERTIES_PREFIX + ".model");
    }

}
