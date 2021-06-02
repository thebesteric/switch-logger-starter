package io.github.thebesteric.framework.switchlogger.core.processor.attribute;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * ResourceAttributeProcessor
 *
 * Handle annotations that contain @Resource in the parent class
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-15 00:21
 * @since 1.0
 */
public class ResourceAttributeProcessor extends ApplicationAttributeProcessor {

    private Field sourceField;

    @Override
    public boolean supports(Field sourceField) {
        this.sourceField = sourceField;
        return sourceField.isAnnotationPresent(Resource.class);
    }

    @Override
    public void processor(Field targetField, Object bean) throws Throwable {
        targetField.setAccessible(true);
        targetField.set(bean, applicationContext.getBean(sourceField.getName()));
    }
}
