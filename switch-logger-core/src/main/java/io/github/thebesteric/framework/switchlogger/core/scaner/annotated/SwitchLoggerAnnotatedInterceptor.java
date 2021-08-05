package io.github.thebesteric.framework.switchlogger.core.scaner.annotated;

import io.github.thebesteric.framework.switchlogger.annotation.SwitchLogger;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.processor.GlobalResponseProcessor;
import io.github.thebesteric.framework.switchlogger.core.processor.RecordProcessor;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import io.github.thebesteric.framework.switchlogger.utils.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * SwitchLoggerAnnotatedInterceptor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-10 02:18
 * @since 1.0
 */
@RequiredArgsConstructor
public class SwitchLoggerAnnotatedInterceptor implements MethodInterceptor {

    private final SwitchLoggerProperties properties;
    private final List<RecordProcessor> recordProcessors;
    private final GlobalResponseProcessor globalResponseProcessor;

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        final String trackId = SwitchLoggerFilterWrapper.trackIdThreadLocal.get();
        SwitchLoggerAnnotationInfo switchLoggerAnnotationInfo = extractSwitchLoggerAnnotationInfo(method);
        String exception = null;
        Object result = null;
        long startTime = System.currentTimeMillis(), durationTime;
        try {
            result = methodProxy.invokeSuper(obj, args);
            exception = globalResponseProcessor.processor(result);
            return result;
        } catch (Throwable throwable) {
            exception = throwable.getMessage();
            throw throwable;
        } finally {
            durationTime = System.currentTimeMillis() - startTime;
            // package log
            InvokeLog invokeLog = new InvokeLog.Builder()
                    .setCreatedTime(System.currentTimeMillis())
                    .setTag(switchLoggerAnnotationInfo.getTag())
                    .setExtra(switchLoggerAnnotationInfo.getExtra())
                    .setResult(result)
                    .setTrackId(trackId)
                    .setException(exception)
                    .setExecuteInfo(new InvokeLog.ExecuteInfo(method, args, startTime, durationTime))
                    .setLevel(exception != null ? InvokeLog.LEVEL_ERROR : switchLoggerAnnotationInfo.getLevel())
                    .build();

            // process log
            if (!properties.isAsync()) {
                execute(invokeLog);
            } else {
                CompletableFuture.runAsync(() -> execute(invokeLog));
            }
        }
    }

    private void execute(final InvokeLog invokeLog) {
        for (RecordProcessor recordProcessor : recordProcessors) {
            try {
                if (recordProcessor.supports(properties.getModel())) {
                    recordProcessor.processor(invokeLog);
                    break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private SwitchLoggerAnnotationInfo extractSwitchLoggerAnnotationInfo(Method method) {
        SwitchLoggerAnnotationInfo.SwitchLoggerAnnotationInfoBuilder builder = SwitchLoggerAnnotationInfo.builder();
        SwitchLogger switchLogger;
        // Check if the class contains @SwitchLogger
        if (method.getDeclaringClass().isAnnotationPresent(SwitchLogger.class)) {
            switchLogger = method.getDeclaringClass().getDeclaredAnnotation(SwitchLogger.class);
            builder.classTag(switchLogger.tag()).classExtra(switchLogger.extra()).level(switchLogger.level());
        }
        // Check if the method contains @SwitchLogger
        if (method.isAnnotationPresent(SwitchLogger.class)) {
            switchLogger = method.getDeclaredAnnotation(SwitchLogger.class);
            builder.methodTag(switchLogger.tag()).methodExtra(switchLogger.extra()).level(switchLogger.level());
        }

        // default tag & level
        builder.defaultTag(StringUtils.lowerFirst(method.getDeclaringClass().getSimpleName())).defaultLevel(SwitchLogger.Level.INFO);

        return builder.build();
    }


    @Getter
    @Setter
    @Builder
    private static class SwitchLoggerAnnotationInfo {

        private String classTag, methodTag, defaultTag, defaultLevel, level;
        private String[] classExtra, methodExtra;

        // Method tag first
        public String getTag() {
            return !StringUtils.isEmpty(methodTag) ? methodTag : !StringUtils.isEmpty(classTag) ? classTag : defaultTag;
        }

        // Method extra first
        public String[] getExtra() {
            return methodExtra != null ? methodExtra : classExtra;
        }

        public String getLevel() {
            return !StringUtils.isEmpty(level) ? level : defaultLevel;
        }

    }


}
