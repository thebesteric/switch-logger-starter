package io.github.thebesteric.framework.switchlogger.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ReflectUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-10 21:43
 * @since 1.0
 */
public class ReflectUtils {

    public static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public static boolean isPublic(Field field) {
        return Modifier.isPublic(field.getModifiers());
    }

    public static boolean isPrivate(Class<?> clazz) {
        return Modifier.isPrivate(clazz.getModifiers());
    }

    public static boolean isPrivate(Method method) {
        return Modifier.isPrivate(method.getModifiers());
    }

    public static boolean isPrivate(Field field) {
        return Modifier.isPrivate(field.getModifiers());
    }

    public static boolean isProtected(Class<?> clazz) {
        return Modifier.isProtected(clazz.getModifiers());
    }

    public static boolean isProtected(Method method) {
        return Modifier.isProtected(method.getModifiers());
    }

    public static boolean isProtected(Field field) {
        return Modifier.isProtected(field.getModifiers());
    }

    public static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    public static boolean isFinal(Method method) {
        return Modifier.isFinal(method.getModifiers());
    }

    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    public static String[] getModifiers(Class<?> clazz) {
        return Modifier.toString(clazz.getModifiers()).split(" ");
    }

    public static String[] getModifiers(Method method) {
        return Modifier.toString(method.getModifiers()).split(" ");
    }

    public static String[] getModifiers(Field field) {
        return Modifier.toString(field.getModifiers()).split(" ");
    }

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (isStatic(field) || isFinal(field)) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    public static List<String> getFieldName(Class<?> clazz) {
        List<Field> fields = getFields(clazz);
        List<String> fieldNames = new ArrayList<>();
        for (Field field : fields) {
            if (isStatic(field) || isFinal(field)) {
                continue;
            }
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> objectClass, Class<T> annotationClass) {
        return objectClass.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public static boolean anyAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass) {
        if (isAnnotationPresent(objectClass, annotationClass)) {
            return true;
        } else {
            for (Method method : objectClass.getDeclaredMethods()) {
                if (isAnnotationPresent(method, annotationClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass) {
        return objectClass.isAnnotationPresent(annotationClass);
    }

    public static boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass);
    }

    public static Constructor<?> determineConstructor(Class<?> clazz) {
        Constructor<?>[] rawCandidates = clazz.getDeclaredConstructors();
        List<Constructor<?>> constructors = Arrays.asList(rawCandidates);
        constructors.sort((o1, o2) -> {
            if (o1.getParameterCount() != o2.getParameterCount()) {
                return o1.getParameterCount() > o2.getParameterCount() ? 1 : -1;
            }
            return 0;
        });
        return constructors.get(0);
    }

    public static Object newInstance(Constructor<?> constructor) throws Throwable {
        constructor.setAccessible(true);
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = ObjectUtils.initialValue(parameters[i].getType());
        }
        return constructor.newInstance(args);
    }


}
