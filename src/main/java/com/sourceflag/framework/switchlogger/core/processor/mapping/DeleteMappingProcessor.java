package com.sourceflag.framework.switchlogger.core.processor.mapping;

import com.sourceflag.framework.switchlogger.core.processor.MappingProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.lang.reflect.Method;

/**
 * DeleteMappingProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 02:25
 * @since 1.0
 */
public class DeleteMappingProcessor implements MappingProcessor {

    private Method method;

    @Override
    public boolean supports(Method method) {
        this.method = method;
        return method.getAnnotation(DeleteMapping.class) != null;
    }

    @Override
    public void processor(String[] classRequestMappingUrls) {
        if (classRequestMappingUrls != null && classRequestMappingUrls.length > 0) {
            for (String classRequestMappingUrl : classRequestMappingUrls) {
                String[] methodRequestMappingUrls = method.getAnnotation(DeleteMapping.class).value();
                for (String methodRequestMappingUrl : methodRequestMappingUrls) {
                    String url = classRequestMappingUrl + methodRequestMappingUrl;
                    SwitchLoggerFilterWrapper.URL_MAPPING.put(url, method);
                }
            }
        }
    }
}
