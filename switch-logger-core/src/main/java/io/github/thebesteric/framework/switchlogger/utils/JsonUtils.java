package io.github.thebesteric.framework.switchlogger.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
            .setSerializationInclusion(JsonInclude.Include.ALWAYS);

}
