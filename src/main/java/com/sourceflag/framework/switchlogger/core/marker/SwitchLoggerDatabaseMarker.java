package com.sourceflag.framework.switchlogger.core.marker;

import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * SwitchLoggerDatabaseMarker
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-30 23:09
 * @since 1.0
 */
public class SwitchLoggerDatabaseMarker implements SwitchLoggerMarker {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(getLoggerModel(context));
    }

}
