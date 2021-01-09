package com.sourceflag.framework.switchlogger.core.processor.logger;

import com.sourceflag.framework.switchlogger.core.domain.RequestLog;
import com.sourceflag.framework.switchlogger.core.processor.RequestLoggerProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;
import com.sourceflag.framework.switchlogger.utils.DurationWatch;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * DefaultRequestLoggerProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-16 23:10
 * @since 1.0
 */
@Slf4j
public class DefaultRequestLoggerProcessor implements RequestLoggerProcessor {
    @Override
    public RequestLog processor(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper, Map<String, Method> mapping, ThreadLocal<String> trackIdThreadLocal, long duration) throws IOException {
        RequestLog requestLog = new RequestLog(requestWrapper, responseWrapper, trackIdThreadLocal);
        try {
            requestLog.setResult(JsonUtils.mapper.readTree(requestLog.getResult().toString()));
        } catch (Exception ex) {
            log.debug("Cannot parse {} to json", requestLog.getResult());
        }
        Method method = mapping.get(requestLog.getUri());
        if (method != null) {
            determineSwitchLoggerInfo(method, requestLog);
            requestLog.setExecuteInfo(new RequestLog.ExecuteInfo(method, null, DurationWatch.getStartTime(), duration));
        }
        return requestLog;
    }
}
