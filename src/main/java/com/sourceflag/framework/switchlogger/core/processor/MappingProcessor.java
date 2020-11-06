package com.sourceflag.framework.switchlogger.core.processor;

import java.lang.reflect.Method;

public interface MappingProcessor {
    boolean supports(Method method);

    void processor(String[] classRequestMappingUrl);
}
