package com.sourceflag.framework.switchlogger.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JsonUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-11-01 00:13
 * @since 1.0
 */
public class JsonUtils {

    public static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

}
