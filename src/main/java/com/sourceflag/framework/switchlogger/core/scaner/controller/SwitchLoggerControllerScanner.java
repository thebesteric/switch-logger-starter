package com.sourceflag.framework.switchlogger.core.scaner.controller;

import com.sourceflag.framework.switchlogger.core.processor.MappingProcessor;
import com.sourceflag.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;

/**
 * SwitchLoggerControllerScanner
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-10 16:55
 * @since 1.0
 */
public class SwitchLoggerControllerScanner implements SwitchLoggerScanner {

    private final List<MappingProcessor> mappingProcessors;

    public SwitchLoggerControllerScanner(List<MappingProcessor> mappingProcessors){
        this.mappingProcessors = mappingProcessors;
    }

    @Override
    public void processClassFile(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
                String[] classRequestMappingUrls = null;
                if (classRequestMapping != null) {
                    classRequestMappingUrls = classRequestMapping.value();
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    for (MappingProcessor mappingProcessor : mappingProcessors) {
                        if (mappingProcessor.supports(method)) {
                            mappingProcessor.processor(classRequestMappingUrls);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
