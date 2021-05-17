package com.sourceflag.framework.switchlogger.db.mysql.configuration.mark;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.configuration.marker.SwitchLoggerConditionMarker;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * SwitchLoggerDatabaseMarker
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-30 23:09
 * @since 1.0
 */
public class SwitchLoggerDatabaseMarker implements SwitchLoggerConditionMarker {

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        return SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(getLoggerModel(context));
    }

}
