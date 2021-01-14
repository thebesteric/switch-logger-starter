package com.sourceflag.framework.switchlogger.core.processor.attribute;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;

/**
 * AutowiredAttributeProcessor
 *
 * Handle annotations that contain @Autowired in the parent class
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-09 17:35
 * @since 1.0
 */
public class AutowiredAttributeProcessor extends ApplicationAttributeProcessor {

    private Field sourceField;

    @Override
    public boolean supports(Field sourceField) {
        this.sourceField = sourceField;
        return sourceField.isAnnotationPresent(Autowired.class);
    }

    @Override
    public void processor(Field targetField, Object bean) throws Throwable {
        targetField.setAccessible(true);
        targetField.set(bean, applicationContext.getBean(sourceField.getName()));
    }
}
