package io.github.thebesteric.framework.switchlogger.core.processor.attribute;

import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;

/**
 * ValueAttributeProcessor
 *
 * Handle SpEL or Environment attr in the parent class
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-09 17:44
 * @since 1.0
 */
public class ValueAttributeProcessor extends ApplicationAttributeProcessor {

    private Field sourceField;

    @Override
    public boolean supports(Field sourceField) {
        this.sourceField = sourceField;
        return sourceField.isAnnotationPresent(Value.class);
    }

    @Override
    public void processor(Field targetField, Object bean) throws Throwable {
        targetField.setAccessible(true);
        Value valueAnnotation = sourceField.getAnnotation(Value.class);
        String value = valueAnnotation.value();
        String propertyValue = value.substring(2, value.length() - 1);
        String[] properties = propertyValue.split(":");
        // Processing environment value
        if (value.startsWith("$")) {
            String property = environment.getProperty(properties[0]);
            if (property == null) {
                if (properties.length >= 2) {
                    property = properties[1];
                }
            }
            targetField.set(bean, property);
        }
        // Processing SpEL value
        else if (value.startsWith("#")) {
            String property = properties[0];
            String[] objectProperties = property.split(".");
            if (objectProperties.length > 0) {
                Object object = applicationContext.getBean(objectProperties[0]);
                Object currentObject = object;
                // eg: #{bean.foo.bar}
                if (objectProperties.length > 1) {
                    for (int i = 1; i < objectProperties.length; i++) {
                        String fieldName = objectProperties[i];
                        for (Field declaredField : object.getClass().getDeclaredFields()) {
                            if (fieldName.equals(declaredField.getName())) {
                                currentObject = declaredField.get(object);
                                break;
                            }
                        }
                    }
                }
                targetField.set(bean, currentObject);
            }
        }

    }

}
