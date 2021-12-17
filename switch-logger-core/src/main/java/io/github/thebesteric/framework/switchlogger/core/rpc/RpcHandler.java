package io.github.thebesteric.framework.switchlogger.core.rpc;

import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import io.github.thebesteric.framework.switchlogger.core.processor.RecordProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RpcHandler extends feign.Logger implements ApplicationContextAware {

    protected static ConfigurableApplicationContext applicationContext;

    protected static SwitchLoggerProperties properties;

    protected static List<RecordProcessor> recordProcessors = new ArrayList<>();

    public static boolean initialized = false;

    @PostConstruct
    public void init() {
        if (!initialized) {
            RpcHandler.properties = applicationContext.getBean(SwitchLoggerProperties.class);
            Map<String, RecordProcessor> recordProcessorMap = applicationContext.getBeansOfType(RecordProcessor.class);
            if (recordProcessorMap.size() > 0) {
                recordProcessorMap.forEach((name, recordProcessor) -> {
                    RpcHandler.recordProcessors.add(recordProcessor);
                });
            }
            initialized = true;
        }
    }

    protected void recordRequestLog(RequestLog requestLog) throws Throwable {
        for (RecordProcessor recordProcessor : recordProcessors) {
            if (recordProcessor.supports(properties.getModel())) {
                recordProcessor.processor(requestLog);
                break;
            }
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        if (!initialized) {
            RpcHandler.applicationContext = (ConfigurableApplicationContext) applicationContext;
        }
    }

    @Override
    protected void log(String s, String s1, Object... objects) {

    }
}
