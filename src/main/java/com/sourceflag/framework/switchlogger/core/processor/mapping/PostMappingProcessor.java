package com.sourceflag.framework.switchlogger.core.processor.mapping;

import com.sourceflag.framework.switchlogger.core.processor.MappingProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
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
        return method.getAnnotation(PostMapping.class) != null;
    }

    @Override
    public void processor(String[] classRequestMappingUrls) {
        if (classRequestMappingUrls != null && classRequestMappingUrls.length > 0) {
            for (String classRequestMappingUrl : classRequestMappingUrls) {
                String[] methodRequestMappingUrls = method.getAnnotation(PostMapping.class).value();
                for (String methodRequestMappingUrl : methodRequestMappingUrls) {
                    String url = classRequestMappingUrl + methodRequestMappingUrl;
                    SwitchLoggerFilterWrapper.URL_MAPPING.put(url, method);
                }
            }
        }
    }
}
