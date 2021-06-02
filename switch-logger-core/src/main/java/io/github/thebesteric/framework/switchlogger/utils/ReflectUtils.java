package io.github.thebesteric.framework.switchlogger.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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

    public static String[] getModifiers(Method method) {
        return Modifier.toString(method.getModifiers()).split(" ");
    }

    public static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static boolean isFinal(Method method) {
        return Modifier.isFinal(method.getModifiers());
    }

    public static String[] getModifiers(Class<?> clazz) {
        return Modifier.toString(clazz.getModifiers()).split(" ");
    }

    public static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    public static String[] getModifiers(Field field) {
        return Modifier.toString(field.getModifiers()).split(" ");
    }

    public static boolean isPublic(Field field) {
        return Modifier.isPublic(field.getModifiers());
    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
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

    public static boolean isAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass) {
        if (objectClass.isAnnotationPresent(annotationClass)) {
            return true;
        } else {
            for (Method method : objectClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    return true;
                }
            }
        }
        return false;
    }

}
