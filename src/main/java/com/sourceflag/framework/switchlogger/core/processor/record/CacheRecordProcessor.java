package com.sourceflag.framework.switchlogger.core.processor.record;

import com.github.benmanes.caffeine.cache.Cache;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * CacheRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-03 01:37
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CacheRecordProcessor extends AbstractSingleThreadRecordProcessor {

    @Qualifier("switchLoggerCache")
    private final Cache<String, Object> cache;

    @Override
    public boolean supports(String model) {
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.CACHE.name().equalsIgnoreCase(model);
    }

    @Override
    public void doProcess(InvokeLog invokeLog) throws Throwable {
        cache.put(invokeLog.getTrackId(), invokeLog);
    }
}
