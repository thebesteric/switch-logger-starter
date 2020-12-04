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
public interface RequestLoggerProcessor {
    RequestLog processor(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper, Map<String, Method> mapping, long duration) throws IOException;
}
