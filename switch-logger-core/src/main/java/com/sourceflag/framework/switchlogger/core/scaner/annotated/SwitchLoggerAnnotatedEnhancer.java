package com.sourceflag.framework.switchlogger.core.scaner.annotated;

import com.sourceflag.framework.switchlogger.annotation.SwitchLogger;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.processor.AttributeProcessor;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    private final Enhancer enhancer = new Enhancer();

    private final ConfigurableListableBeanFactory beanFactory;
    private final SwitchLoggerProperties properties;
    private final List<RecordProcessor> recordProcessors;
    private final List<AttributeProcessor> attributeProcessors;

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // Not proxy layer
        if ((beanClass.isAnnotationPresent(Controller.class) || beanClass.isAnnotationPresent(RestController.class))
                && !ReflectUtils.isAnnotationPresent(beanClass, SwitchLogger.class)) {
            return bean;
        }

        String originClassName = getOriginClassName(beanClass);

        try {
            Class<?> originClass = Class.forName(originClassName);

            // if Class has @SwitchLogger
            if (originClass.isAnnotationPresent(SwitchLogger.class)) {
                if (checkLegal(originClass)) {
                    return enhancer(originClass, new SwitchLoggerAnnotatedInterceptor(properties, recordProcessors), beanName, bean);
                }
            }

            // if Method has @SwitchLogger
            for (Method declaredMethod : beanClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(SwitchLogger.class)) {
                    if (checkLegal(declaredMethod)) {
                        return enhancer(originClass, new SwitchLoggerAnnotatedInterceptor(properties, recordProcessors), beanName, bean);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * Enhancer class
     *
     * @param originClass originClass
     * @param callback    callback function
     * @param beanName    beanName
     * @param bean        bean
     * @return java.lang.Object
     * @author Eric
     * @date 2021/1/14 23:34
     */
    private Object enhancer(Class<?> originClass, Callback callback, String beanName, Object bean) {
        enhancer.setSuperclass(originClass);
        enhancer.setCallback(callback);

        Object object = null;

        // 1. Deal the no argument constructor
        for (Constructor<?> constructor : originClass.getDeclaredConstructors()) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length == 0) {
                object = enhancer.create();
            }
        }

        // 2. Deal the has argument constructor
        if (object == null) {
            // TODO constructors maybe sort by parameters
            for (Constructor<?> constructor : originClass.getDeclaredConstructors()) {
                Parameter[] parameters = constructor.getParameters();
                if (parameters.length != 0) {
                    Class<?>[] argumentTypes = new Class<?>[parameters.length];
                    Object[] arguments = new Object[parameters.length];
                    boolean isCreate = true;
                    for (int i = 0; i < parameters.length; i++) {
                        Class<?> clazz = parameters[i].getType();
                        try {
                            arguments[i] = beanFactory.getBean(clazz);
                            argumentTypes[i] = clazz;
                        } catch (NoSuchBeanDefinitionException ex) {
                            isCreate = false;
                            break;
                        }
                    }
                    if (isCreate) {
                        object = enhancer.create(argumentTypes, arguments);
                        break;
                    }
                }
            }
        }

        if (object != null) {
            // Begin processing legal properties of the class
            handleAttributes(originClass, object);
            beanFactory.registerSingleton(beanName, object);
        }

        // TODO maybe has problem
        // return object == null ? bean : object;
        return bean;
    }

    /**
     * handleParentAttributes
     * <p>
     * Processing legal properties of the parent class
     * eg. @Autowired or @Value
     *
     * @param originClass originClass
     * @param proxyObject proxyObject
     * @author Eric
     * @date 2021/1/14 23:40
     */
    private void handleAttributes(Class<?> originClass, Object proxyObject) {
        Class<?> currentClass = originClass;
        do {
            Field[] currentClassDeclaredFields = currentClass.getDeclaredFields();
            injectAttributes(currentClassDeclaredFields, proxyObject);
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);
    }

    /**
     * injectAttributes
     * <p>
     * eg. @Autowired, @Resource, @Value
     *
     * @param declaredFields declaredFields
     * @param proxyObject    proxyObject
     * @author Eric
     * @date 2021/1/14 23:45
     */
    private void injectAttributes(Field[] declaredFields, Object proxyObject) {
        for (Field declaredField : declaredFields) {
            boolean isFinal = ReflectUtils.isFinal(declaredField);
            boolean isStatic = ReflectUtils.isStatic(declaredField);
            if (!isFinal && !isStatic) {
                for (AttributeProcessor attributeProcessor : attributeProcessors) {
                    if (attributeProcessor.supports(declaredField)) {
                        try {
                            attributeProcessor.processor(declaredField, proxyObject);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * checkLegal by Method
     * <p>
     * The method type must be public and not static and final
     *
     * @param method method
     * @return boolean
     * @author Eric
     * @date 2021/1/14 23:30
     */
    private boolean checkLegal(Method method) {
        return ReflectUtils.isPublic(method) && !ReflectUtils.isStatic(method) && !ReflectUtils.isFinal(method);
    }

    /**
     * checkLegal by ClassType
     * <p>
     * The class type must be public and not static and final
     *
     * @param clazz clazz
     * @return boolean
     * @author Eric
     * @date 2021/1/14 23:30
     */
    private boolean checkLegal(Class<?> clazz) {
        return ReflectUtils.isPublic(clazz) && !ReflectUtils.isStatic(clazz) && !ReflectUtils.isFinal(clazz);
    }

    /**
     * 获取 bean 的 class
     *
     * @param beanClass beanClass
     * @return java.lang.String
     * @author Eric
     * @date 2021/5/14 0:42
     */
    private String getOriginClassName(Class<?> beanClass) {
        String beanClassName = beanClass.getName();
        String originClassName = beanClassName;

        // Check if used Spring CGLIB. eg: Aspect
        if (beanClassName.contains(SPRING_CGLIB_SEPARATOR)) {
            originClassName = beanClassName.substring(0, beanClassName.indexOf(SPRING_CGLIB_SEPARATOR));
        }
        return originClassName;
    }

}
