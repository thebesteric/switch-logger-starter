package com.sourceflag.framework.switchlogger.core.scaner.annotated;

import com.sourceflag.framework.switchlogger.annotation.SwitchLogger;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
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

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        final String trackId = SwitchLoggerFilterWrapper.trackIdThreadLocal.get();
        SwitchLoggerAnnotationInfo switchLoggerAnnotationInfo = extractSwitchLoggerAnnotationInfo(method);
        String exception = null;
        Object result = null;
        long startTime = System.currentTimeMillis(), durationTime = 0;
        try {
            result = methodProxy.invokeSuper(obj, args);
            durationTime = System.currentTimeMillis() - startTime;
            return result;
        } catch (Throwable throwable) {
            durationTime = System.currentTimeMillis() - startTime;
            exception = throwable.getMessage();
            throw throwable;
        } finally {
            Object finalResult = result;
            String finalException = exception;
            long finalDurationTime = durationTime;
            CompletableFuture.runAsync(() -> {
                InvokeLog invokeLog = new InvokeLog();
                invokeLog.setCreatedTime(System.currentTimeMillis());
                invokeLog.setTag(switchLoggerAnnotationInfo.getAnnotationTag());
                invokeLog.setExtra(switchLoggerAnnotationInfo.getAnnotationExtra());
                invokeLog.setResult(finalResult);
                invokeLog.setTrackId(trackId);
                invokeLog.setException(finalException);
                invokeLog.setExecuteInfo(new InvokeLog.ExecuteInfo(method, startTime, finalDurationTime));
                if (finalException != null) {
                    invokeLog.setType(InvokeLog.TYPE_ERROR);
                }

                // process log
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
            });
        }
    }

    private SwitchLoggerAnnotationInfo extractSwitchLoggerAnnotationInfo(Method method) {
        SwitchLoggerAnnotationInfo.SwitchLoggerAnnotationInfoBuilder builder = SwitchLoggerAnnotationInfo.builder();
        if (method.getDeclaringClass().isAnnotationPresent(SwitchLogger.class)) {
            SwitchLogger classAnnotation = method.getDeclaringClass().getDeclaredAnnotation(SwitchLogger.class);
            builder.classAnnotationTag(classAnnotation.tag()).classAnnotationExtra(classAnnotation.extra());
        }
        if (method.isAnnotationPresent(SwitchLogger.class)) {
            SwitchLogger methodAnnotation = method.getDeclaredAnnotation(SwitchLogger.class);
            builder.methodAnnotationTag(methodAnnotation.tag()).methodAnnotationExtra(methodAnnotation.extra());
        }

        return builder.build();
    }


    @Getter
    @Setter
    @Builder
    private static class SwitchLoggerAnnotationInfo {

        String classAnnotationTag, methodAnnotationTag;
        String[] classAnnotationExtra, methodAnnotationExtra;

        public String getAnnotationTag() {
            return methodAnnotationTag == null ? classAnnotationTag : methodAnnotationTag;
        }

        public String[] getAnnotationExtra() {
            return methodAnnotationExtra == null ? classAnnotationExtra : methodAnnotationExtra;
        }

    }


}