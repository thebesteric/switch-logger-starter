package io.github.thebesteric.framework.switchlogger.core.processor.mapping;

import io.github.thebesteric.framework.switchlogger.core.processor.MappingProcessor;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

/**
 * PostMappingProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 02:25
 * @since 1.0
 */
public class PostMappingProcessor implements MappingProcessor {

    private Method method;

    @Override
    public boolean supports(Method method) {
        this.method = method;
        return method.isAnnotationPresent(PostMapping.class);
    }

    @Override
    public void processor(String[] classRequestMappingUrls) {
        if (classRequestMappingUrls != null && classRequestMappingUrls.length > 0) {
            for (String classRequestMappingUrl : classRequestMappingUrls) {
                String[] methodRequestMappingUrls = method.getAnnotation(PostMapping.class).value();
                doProcessor(classRequestMappingUrl, methodRequestMappingUrls, method);
            }
        }
    }
}
