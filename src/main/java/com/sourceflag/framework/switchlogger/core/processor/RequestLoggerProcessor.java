package com.sourceflag.framework.switchlogger.core.processor;

import com.sourceflag.framework.switchlogger.core.RequestLog;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * LoggerProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-03 01:09
 * @since 1.0
 */
@FunctionalInterface
public interface RequestLoggerProcessor {

    /**
     * RequestLoggerProcessor
     *
     * @param requestWrapper  requestWrapper
     * @param responseWrapper responseWrapper
     * @param mapping         mapping
     * @param duration        duration
     * @return com.sourceflag.framework.switchlogger.core.RequestLog
     * @author Eric
     * @date 2020/12/9 17:04
     */
    RequestLog processor(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper, Map<String, Method> mapping, long duration) throws IOException;
}
