package io.github.thebesteric.framework.switchlogger.core.processor;

import java.lang.reflect.Field;

/**
 * AttributeProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-01-09 17:30
 * @since 1.0
 */
public interface AttributeProcessor {

    boolean supports(Field sourceField);

    void processor(Field targetField, Object bean) throws Throwable;

}
