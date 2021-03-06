package io.github.thebesteric.framework.switchlogger.core.wrapper;

import io.github.thebesteric.framework.switchlogger.utils.TransactionUtils;

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
public abstract class SwitchLoggerFilterWrapper implements Filter {

    public static final Map<String, Method> URL_MAPPING = new ConcurrentHashMap<>(128);

    public static final ThreadLocal<String> trackIdThreadLocal = TransactionUtils.create();

    protected void initTrackId(SwitchLoggerRequestWrapper requestWrapper) {
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
