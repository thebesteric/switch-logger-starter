package com.sourceflag.framework.switchlogger.core.processor;

import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;

import java.lang.reflect.Method;

/**
 * MappingProcessor
 *
 * @author Eric
 * @date 2020/12/9 17:13
 */
public interface MappingProcessor {
    /**
     * supports
     *
     * @param method method
     * @return boolean
     * @author Eric
     * @date 2020/12/9 17:13
     */
    boolean supports(Method method);

    /**
     * processor
     *
     * @param classRequestMappingUrl classRequestMappingUrl
     * @author Eric
     * @date 2020/12/9 17:13
     */
    void processor(String[] classRequestMappingUrl);


    default void doProcessor(String classRequestMappingUrl, String[] methodRequestMappingUrls, Method method) {
        if (methodRequestMappingUrls.length == 0) {
            SwitchLoggerFilterWrapper.URL_MAPPING.put(classRequestMappingUrl, method);
            if (!classRequestMappingUrl.endsWith("/")) {
                SwitchLoggerFilterWrapper.URL_MAPPING.put(classRequestMappingUrl + "/", method);
            }
        } else {
            for (String methodRequestMappingUrl : methodRequestMappingUrls) {
                String url = classRequestMappingUrl + methodRequestMappingUrl;
                SwitchLoggerFilterWrapper.URL_MAPPING.put(url, method);
            }
        }
    }
}
