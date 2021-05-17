package com.sourceflag.framework.switchlogger.utils;

public class StringUtils {

    public static boolean isAlpha(String str) {
        if (str == null) return false;
        return str.matches("[a-zA-Z]+");
    }


    public static String lowerFirst(String str) {
        if (isAlpha(str) && !Character.isLowerCase(str.charAt(0))) {
            char[] chars = str.toCharArray();
            chars[0] += 32;
            return String.valueOf(chars);
        }
        return str;
    }

    public static String upperFirst(String str) {
        if (isAlpha(str) && !Character.isUpperCase(str.charAt(0))) {
            char[] chars = str.toCharArray();
            chars[0] -= 32;
            return String.valueOf(chars);
        }
        return str;
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

}
