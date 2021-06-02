package io.github.thebesteric.framework.switchlogger.core.processor.attribute;

import io.github.thebesteric.framework.switchlogger.core.processor.AttributeProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

/**
 * ApplicationAttributeProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-09 17:41
 * @since 1.0
 */
public abstract class ApplicationAttributeProcessor implements AttributeProcessor, ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected Environment environment;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getBean(Environment.class);
    }
}
