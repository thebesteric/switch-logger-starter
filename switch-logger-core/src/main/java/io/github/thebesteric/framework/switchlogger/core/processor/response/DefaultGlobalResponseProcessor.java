package io.github.thebesteric.framework.switchlogger.core.processor.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.processor.GlobalResponseProcessor;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import io.github.thebesteric.framework.switchlogger.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultGlobalResponseProcessor implements GlobalResponseProcessor {

    private final SwitchLoggerProperties properties;

    @Override
    public String processor(Object result) {
        SwitchLoggerProperties.GlobalResponse globalResponse = properties.getGlobalResponse();
        if (globalResponse != null && !StringUtils.isEmpty(globalResponse.getCodeField())) {
            try {
                String resultJsonStr = JsonUtils.mapper.writeValueAsString(result);
                JsonNode resultJsonNode = JsonUtils.mapper.readTree(resultJsonStr);
                String codeField = globalResponse.getCodeField();
                JsonNode jsonCodeField = getJsonNodeField(resultJsonNode, codeField);
                if (jsonCodeField != null && properties.getGlobalResponse().getSucceedCode() != jsonCodeField.asInt()) {
                    String messageField = properties.getGlobalResponse().getMessageField();
                    JsonNode jsonMessageField = getJsonNodeField(resultJsonNode, messageField);
                    return jsonMessageField != null ? jsonMessageField.asText() : resultJsonStr;
                }
            } catch (JsonProcessingException ex) {
                log.warn(ex.getMessage());
            }
        }
        return null;
    }
}
