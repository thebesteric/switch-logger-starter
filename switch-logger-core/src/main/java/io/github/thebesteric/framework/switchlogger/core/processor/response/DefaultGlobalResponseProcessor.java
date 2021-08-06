package io.github.thebesteric.framework.switchlogger.core.processor.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.processor.GlobalResponseProcessor;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DefaultGlobalResponseProcessor implements GlobalResponseProcessor {

    private final SwitchLoggerProperties properties;

    @Override
    public String processor(Object result) {
        SwitchLoggerProperties.GlobalResponse globalResponse = properties.getGlobalResponse();
        if (globalResponse != null) {
            try {
                String resultJsonStr = JsonUtils.mapper.writeValueAsString(result);
                JsonNode resultJsonNode = JsonUtils.mapper.readTree(resultJsonStr);
                List<SwitchLoggerProperties.GlobalResponse.ResponseEntity> responseEntities = globalResponse.getResponseEntities();

                // Check whether a match is found. If a match is found, it indicates normal
                for (SwitchLoggerProperties.GlobalResponse.ResponseEntity responseEntity : responseEntities) {
                    String codeField = responseEntity.getCodeField();
                    JsonNode jsonCodeField = getJsonNodeField(resultJsonNode, codeField);
                    if (jsonCodeField.asText().equals(responseEntity.getCodeValue())) {
                        return null;
                    }
                }

                // Found exception in result
                for (String errorMessageField : globalResponse.getErrorMessageFields()) {
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
