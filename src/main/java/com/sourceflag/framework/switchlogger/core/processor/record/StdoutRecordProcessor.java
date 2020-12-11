package com.sourceflag.framework.switchlogger.core.processor.record;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;

/**
 * StdoutRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-11-29 23:33
 * @since 1.0
 */
public class StdoutRecordProcessor implements RecordProcessor {
    @Override
    public boolean supports(String model) throws UnsupportedModelException {
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.STDOUT.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(InvokeLog invokeLog) throws Throwable {
        System.out.println(JsonUtils.mapper.writeValueAsString(invokeLog));
    }
}
