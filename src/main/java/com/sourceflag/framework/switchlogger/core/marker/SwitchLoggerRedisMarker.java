package com.sourceflag.framework.switchlogger.core.marker;

import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * SwitchLoggerReidsMarker
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-30 23:12
 * @since 1.0
 */
public class SwitchLoggerRedisMarker implements SwitchLoggerMarker {
    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        return SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(getLoggerModel(context));
    }
}
