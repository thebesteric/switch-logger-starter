package com.sourceflag.framework.switchlogger.core.processor.record;

import com.google.common.cache.Cache;
import com.sourceflag.framework.switchlogger.core.RequestLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
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
public class CacheRecordProcessor implements RecordProcessor {

    @Qualifier("switchLoggerCache")
    private final Cache<String, Object> cache;

    @Override
    public boolean supports(String model) {
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.CACHE.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(RequestLog requestLog) throws Exception {
        cache.put(requestLog.getTrackId(), requestLog);
    }
}
