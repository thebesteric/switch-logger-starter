package com.sourceflag.framework.switchlogger.core.processor.logger;

import com.sourceflag.framework.switchlogger.core.domain.RequestLog;
import com.sourceflag.framework.switchlogger.core.processor.RequestLoggerProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * AbstractRequestLoggerProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-16 23:12
 * @since 1.0
 */
@Slf4j
public abstract class AbstractRequestLoggerProcessor implements RequestLoggerProcessor {

    @Override
    public RequestLog processor(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper, Map<String, Method> mapping, ThreadLocal<String> trackIdThreadLocal, long duration) throws IOException {
        RequestLog requestLog = new DefaultRequestLoggerProcessor().processor(requestWrapper, responseWrapper, mapping, trackIdThreadLocal, duration);
        return doAfterProcessor(requestLog);
    }

    public abstract RequestLog doAfterProcessor(RequestLog requestLog);

}
