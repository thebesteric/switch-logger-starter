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
public class StdoutRecordProcessor extends AbstractSingleThreadRecordProcessor {

    public StdoutRecordProcessor(SwitchLoggerProperties properties) {
        super(properties);
    }

    @Override
    public boolean supports(String model) throws UnsupportedModelException {
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.STDOUT.name().equalsIgnoreCase(model);
    }

    @Override
    public void doProcess(InvokeLog invokeLog) throws Throwable {
        String jsonLog = JsonUtils.mapper.writeValueAsString(invokeLog);
        switch (invokeLog.getLevel()) {
            case InvokeLog.LEVEL_ERROR:
            case InvokeLog.LEVEL_WARN:
                System.err.println(jsonLog);
                break;
            default:
                System.out.println(jsonLog);
        }

    }
}
