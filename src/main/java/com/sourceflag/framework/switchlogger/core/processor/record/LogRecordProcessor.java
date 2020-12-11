package com.sourceflag.framework.switchlogger.core.processor.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * LogRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-02 00:50
 * @since 1.0
 */
@Slf4j
public class LogRecordProcessor implements RecordProcessor {

    @Override
    public boolean supports(String model) {
        return model == null || model.trim().equals("") || SwitchLoggerProperties.ModelType.LOG.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(InvokeLog invokeLog) throws JsonProcessingException {
        log.info(JsonUtils.mapper.writeValueAsString(invokeLog));
    }
}
