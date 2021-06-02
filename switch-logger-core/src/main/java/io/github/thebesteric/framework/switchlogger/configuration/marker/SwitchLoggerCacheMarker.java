package io.github.thebesteric.framework.switchlogger.configuration.marker;

import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * SwitchLoggerCacheMarker
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-30 23:12
 * @since 1.0
 */
public class SwitchLoggerCacheMarker implements SwitchLoggerConditionMarker {

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        return SwitchLoggerProperties.ModelType.CACHE.name().equalsIgnoreCase(getLoggerModel(context));
    }
}
