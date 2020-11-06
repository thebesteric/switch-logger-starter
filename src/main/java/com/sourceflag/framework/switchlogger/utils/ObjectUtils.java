package com.sourceflag.framework.switchlogger.utils;

import java.lang.reflect.Field;
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

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static String[] getFieldName(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    public static String humpToUnderline(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
