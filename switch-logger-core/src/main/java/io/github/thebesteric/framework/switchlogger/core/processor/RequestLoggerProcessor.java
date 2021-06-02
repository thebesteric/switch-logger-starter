package io.github.thebesteric.framework.switchlogger.core.processor;

import io.github.thebesteric.framework.switchlogger.annotation.SwitchLogger;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;

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

    /**
     * RequestLoggerProcessor
     *
     * @param requestWrapper     requestWrapper
     * @param responseWrapper    responseWrapper
     * @param mapping            mapping
     * @param trackIdThreadLocal trackIdThreadLocal
     * @param duration           duration
     * @return com.sourceflag.framework.switchlogger.core.domain.RequestLog
     * @author Eric
     * @date 2020/12/9 17:04
     */
    RequestLog processor(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper, Map<String, Method> mapping, ThreadLocal<String> trackIdThreadLocal, long duration) throws IOException;

    /**
     * Determine InvokeLog tag value
     *
     * @param method    method
     * @param invokeLog invokeLog
     * @author Eric
     * @date 2021/1/10 0:46
     */
    default void determineSwitchLoggerInfo(Method method, InvokeLog invokeLog) {
        SwitchLogger switchLogger = null;
        if (method.isAnnotationPresent(SwitchLogger.class)) {
            switchLogger = method.getAnnotation(SwitchLogger.class);
        } else if (method.getDeclaringClass().isAnnotationPresent(SwitchLogger.class)) {
            switchLogger = method.getDeclaringClass().getAnnotation(SwitchLogger.class);
        }
        if (switchLogger != null) {
            if (!switchLogger.tag().trim().isEmpty()) {
                invokeLog.setTag(switchLogger.tag().trim());
            }
            if (switchLogger.extra().length > 0) {
                invokeLog.setExtra(switchLogger.extra());
            }
        }
    }
}
