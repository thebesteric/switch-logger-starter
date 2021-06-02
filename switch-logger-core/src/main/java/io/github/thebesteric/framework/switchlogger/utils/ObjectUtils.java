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

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static String humpToUnderline(String str) {
        if (Character.isUpperCase(str.charAt(0))) {
            str = toLowerCaseFirst(str);
        }

        Matcher matcher = humpPattern.matcher(str);
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

}
