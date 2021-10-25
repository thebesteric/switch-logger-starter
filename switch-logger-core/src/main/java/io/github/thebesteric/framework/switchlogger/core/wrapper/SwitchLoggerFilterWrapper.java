package io.github.thebesteric.framework.switchlogger.core.wrapper;

import io.github.thebesteric.framework.switchlogger.utils.StringUtils;
import io.github.thebesteric.framework.switchlogger.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

import javax.servlet.Filter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SwitchLoggerFilterWrapper
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 00:35
 * @since 1.0
 */
@Slf4j
public abstract class SwitchLoggerFilterWrapper implements Filter {

    public static final Map<String, Method> URL_MAPPING = new ConcurrentHashMap<>(128);

    public static ThreadLocal<String> trackIdThreadLocal = TransactionUtils.create();

    protected void initTrackId(SwitchLoggerRequestWrapper requestWrapper, boolean isSkyWalkingTrace) {
        if (isSkyWalkingTrace) {
            if (StringUtils.isEmpty(TraceContext.traceId()) || "Ignored_Trace".equalsIgnoreCase(TraceContext.traceId())) {
                log.warn("Please check sky walking agent setting are correct or OAP Server are running, used local track id instead");
                TransactionUtils.initialize();
            } else {
                trackIdThreadLocal.set(TraceContext.traceId());
            }
        } else {
            TransactionUtils.initialize();
        }
        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = requestWrapper.getHeader(headerName);
            if (hasTrackId(headerName)) {
                trackIdThreadLocal.set(headerValue);
            }
        }
    }

    private boolean hasTrackId(String headerName) {
        String[] arr = {"track-id", "x-track-id", "transaction-id", "x-transaction-id"};
        for (String key : arr) {
            if (headerName.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

}
