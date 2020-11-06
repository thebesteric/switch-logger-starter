package com.sourceflag.framework.switchlogger.core.wrapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.Filter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SwitchLoggerFilterWrapper
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 00:35
 * @since 1.0
 */
public abstract class SwitchLoggerFilterWrapper implements Filter {

    public static final Map<String, Method> URL_MAPPING = new ConcurrentHashMap<>();

}
