package io.github.thebesteric.framework.switchlogger.utils;

/**
 * 持续时间计算工具类
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-06-28 22:30
 * @since 1.0
 */
public class DurationWatch {

    private static final ThreadLocal<Long> TIME_THREAD_LOCAL = ThreadLocal.withInitial(System::currentTimeMillis);

    public static void start() {
        TIME_THREAD_LOCAL.set(System.currentTimeMillis());
    }

    public static long stop() {
        return System.currentTimeMillis() - TIME_THREAD_LOCAL.get();
    }

    public static long getStartTime() {
        return TIME_THREAD_LOCAL.get();
    }

}
