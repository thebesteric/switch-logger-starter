package io.github.thebesteric.framework.switchlogger.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-31 16:02
 * @since 1.0
 */
public class ObjectUtils {

    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    public static String humpToUnderline(String str) {
        if (Character.isUpperCase(str.charAt(0))) {
            str = toLowerCaseFirst(str);
        }

        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String toLowerCaseFirst(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String toUpperCaseFirst(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static Object initialValue(Class<?> clazz) {
        Object object = null;
        if (clazz.isPrimitive()) {
            if (clazz == char.class) {
                object = '0';
            } else if (clazz == byte.class) {
                object = 0;
            } else if (clazz == short.class) {
                object = 0;
            } else if (clazz == int.class) {
                object = 0;
            } else if (clazz == long.class) {
                object = 0L;
            } else if (clazz == float.class) {
                object = 0.0F;
            } else if (clazz == double.class) {
                object = 0.0D;
            } else if (clazz == boolean.class) {
                object = false;
            }
        }
        return object;
    }

}
