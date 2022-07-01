package io.github.thebesteric.framework.switchlogger.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.lang.NonNull;

import java.util.Arrays;

/**
 * Default Global Success Response
 */
public class DefaultGlobalSuccessResponseInitialize implements SmartInitializingSingleton, ApplicationContextAware {

    private AbstractApplicationContext applicationContext;

    @Override
    public void afterSingletonsInstantiated() {
        SwitchLoggerProperties switchLoggerProperties = applicationContext.getBean(SwitchLoggerProperties.class);
        SwitchLoggerProperties.GlobalSuccessResponse defaultGlobalSuccessResponse = switchLoggerProperties.getGlobalSuccessResponse();
        if (defaultGlobalSuccessResponse != null && defaultGlobalSuccessResponse.isUseDefault()) {
            defaultGlobalSuccessResponse = new SwitchLoggerProperties.GlobalSuccessResponse();
            defaultGlobalSuccessResponse.setUseDefault(true);
            defaultGlobalSuccessResponse.setResponseEntities(Arrays.asList(
                    new SwitchLoggerProperties.GlobalSuccessResponse.ResponseEntity("code", "200"),
                    new SwitchLoggerProperties.GlobalSuccessResponse.ResponseEntity("code", "100")));
            defaultGlobalSuccessResponse.setMessageFields(Arrays.asList("message", "msg"));
            switchLoggerProperties.setGlobalSuccessResponse(defaultGlobalSuccessResponse);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AbstractApplicationContext) applicationContext;
    }
}
