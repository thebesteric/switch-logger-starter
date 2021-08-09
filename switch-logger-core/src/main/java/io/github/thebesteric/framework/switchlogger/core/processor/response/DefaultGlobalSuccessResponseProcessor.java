package io.github.thebesteric.framework.switchlogger.core.processor.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.switchlogger.annotation.SwitchLogger;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.processor.GlobalSuccessResponseProcessor;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import io.github.thebesteric.framework.switchlogger.utils.ReflectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DefaultGlobalSuccessResponseProcessor implements GlobalSuccessResponseProcessor {

    private final SwitchLoggerProperties properties;

    @Override
    public String processor(Method method, Object result) {
        SwitchLoggerProperties.GlobalSuccessResponse globalResponse = properties.getGlobalSuccessResponse();
        if (globalResponse != null) {
            if (method != null) {
                SwitchLogger switchLogger = ReflectUtils.getAnnotation(method, SwitchLogger.class);
                // if @SwitchLogger is disabled
                if (switchLogger != null && !switchLogger.enable()) {
                    return null;
                }
            }
            try {
                String resultJsonStr = JsonUtils.mapper.writeValueAsString(result);
                JsonNode resultJsonNode = JsonUtils.mapper.readTree(resultJsonStr);
                List<SwitchLoggerProperties.GlobalSuccessResponse.ResponseEntity> responseEntities = globalResponse.getResponseEntities();

                // Check whether a match is found. If a match is found, it indicates normal
                for (SwitchLoggerProperties.GlobalSuccessResponse.ResponseEntity responseEntity : responseEntities) {
                    String codeField = responseEntity.getCodeField();
                    JsonNode jsonCodeField = getJsonNodeField(resultJsonNode, codeField);
                    if (jsonCodeField == null || jsonCodeField.asText().equals(responseEntity.getCodeValue())) {
                        return null;
                    }
                }

                // Found exception in result
                for (String errorMessageField : globalResponse.getMessageFields()) {
                    JsonNode jsonMessageField = getJsonNodeField(resultJsonNode, errorMessageField);
                    if (jsonMessageField != null) {
                        return jsonMessageField.asText();
                    }
                }
                return resultJsonStr;
            } catch (JsonProcessingException ex) {
                log.warn(ex.getMessage());
            }
        }
        return null;
    }
}
