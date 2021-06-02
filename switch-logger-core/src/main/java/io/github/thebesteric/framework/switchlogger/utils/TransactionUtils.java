package io.github.thebesteric.framework.switchlogger.utils;

import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;

import java.util.UUID;

/**
 * TransactionUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-26 21:21
 * @since 1.0
 */
public class TransactionUtils {

    public static ThreadLocal<String> create() {
        return create(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
    }

    public static ThreadLocal<String> create(String id) {
        return ThreadLocal.withInitial(() -> id);
    }

    public static String get() {
        return SwitchLoggerFilterWrapper.trackIdThreadLocal.get();
    }

    public static void set(String id) {
        SwitchLoggerFilterWrapper.trackIdThreadLocal.set(id);
    }

}
