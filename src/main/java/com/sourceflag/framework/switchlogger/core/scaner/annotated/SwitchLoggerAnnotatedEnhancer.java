package com.sourceflag.framework.switchlogger.core.scaner.annotated;

import com.sourceflag.framework.switchlogger.annotation.SwitchLogger;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.utils.ReflectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.lang.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * SwitchLoggerEnhancer
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-10 23:47
 * @since 1.0
 */
@RequiredArgsConstructor
public class SwitchLoggerAnnotatedEnhancer implements BeanPostProcessor {

    private static final String SPRING_CGLIB_SEPARATOR = "$$";

    private Enhancer enhancer = new Enhancer();

    private final ConfigurableListableBeanFactory beanFactory;
    private final SwitchLoggerProperties properties;
    private final List<RecordProcessor> recordProcessors;

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        String beanClassName = beanClass.getName();
        String originClassName = beanClassName;

        // check if used Spring CGLIB, eg: Aspect
        if (beanClassName.contains(SPRING_CGLIB_SEPARATOR)) {
            originClassName = beanClassName.substring(0, beanClassName.indexOf(SPRING_CGLIB_SEPARATOR));
        }

        try {
            Class<?> originClass = Class.forName(originClassName);

            // if Class has @SwitchLogger
            if (originClass.isAnnotationPresent(SwitchLogger.class)) {
                if (checkLegal(originClass)) {
                    return enhancer(originClass, new SwitchLoggerAnnotatedInterceptor(properties, recordProcessors), beanName);
                }
            }

            // if Class not has @SwitchLogger, check Method
            for (Method declaredMethod : beanClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(SwitchLogger.class)) {
                    if (checkLegal(declaredMethod)) {
                        return enhancer(originClass, new SwitchLoggerAnnotatedInterceptor(properties, recordProcessors), beanName);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bean;
    }

    private Object enhancer(Class<?> originClass, Callback callback, String beanName) {
        enhancer.setSuperclass(originClass);
        enhancer.setCallback(callback);

        Object object = null;
        for (Constructor<?> constructor : originClass.getDeclaredConstructors()) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length == 0) {
                object = enhancer.create();
            }
        }

        if (object == null) {
            for (Constructor<?> constructor : originClass.getDeclaredConstructors()) {
                Parameter[] parameters = constructor.getParameters();
                if (parameters.length != 0) {
                    Class<?>[] argumentTypes = new Class<?>[parameters.length];
                    Object[] arguments = new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        Class<?> clazz = parameters[0].getType();
                        try {
                            arguments[i] = beanFactory.getBean(clazz);
                        } catch (NoSuchBeanDefinitionException ex) {
                            break;
                        }
                        argumentTypes[i] = clazz;
                    }
                    object = enhancer.create(argumentTypes, arguments);
                    break;
                }
            }
        }

        if (object != null) {
            beanFactory.registerSingleton(beanName, object);
        }

        return object;
    }

    private boolean checkLegal(Method method) {
        return ReflectUtils.isPublic(method) && !ReflectUtils.isStatic(method) && !ReflectUtils.isFinal(method);
    }

    private boolean checkLegal(Class<?> clazz) {
        return ReflectUtils.isPublic(clazz) && !ReflectUtils.isStatic(clazz) && !ReflectUtils.isFinal(clazz);
    }
}
