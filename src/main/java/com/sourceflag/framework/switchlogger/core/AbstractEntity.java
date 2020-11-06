package com.sourceflag.framework.switchlogger.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import lombok.SneakyThrows;

import java.io.Serializable;

/**
 * AbstractEntity
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-31 18:04
 * @since 1.0
 */
public class AbstractEntity implements Serializable {

    @SneakyThrows
    @Override
    public String toString() {
        return JsonUtils.mapper.writeValueAsString(this);
    }
}
