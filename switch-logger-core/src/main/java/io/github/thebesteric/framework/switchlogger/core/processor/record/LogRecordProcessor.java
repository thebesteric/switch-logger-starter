package io.github.thebesteric.framework.switchlogger.core.processor.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
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
public class LogRecordProcessor extends AbstractSingleThreadRecordProcessor {

    public LogRecordProcessor(SwitchLoggerProperties properties) {
        super(properties);
    }

    @Override
    public boolean supports(String model) {
        return model == null || model.trim().equals("") || SwitchLoggerProperties.ModelType.LOG.name().equalsIgnoreCase(model);
    }

    @Override
    public void doProcess(InvokeLog invokeLog) throws JsonProcessingException {
        String jsonLog = JsonUtils.mapper.writeValueAsString(invokeLog);
        switch (invokeLog.getLevel().toUpperCase()) {
            case InvokeLog.LEVEL_INFO:
                log.info(jsonLog);
                break;
            case InvokeLog.LEVEL_WARN:
                log.warn(jsonLog);
                break;
            case InvokeLog.LEVEL_ERROR:
                log.error(jsonLog);
                break;
            default:
                log.debug(jsonLog);
        }
    }
}
