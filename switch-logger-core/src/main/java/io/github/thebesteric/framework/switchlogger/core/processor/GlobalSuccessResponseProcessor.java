package io.github.thebesteric.framework.switchlogger.core.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;

import java.lang.reflect.Method;

/**
 * Return null if there are no exceptions
 */
public interface GlobalSuccessResponseProcessor {

    /**
     * process exception message
     * if the controller is processing then method attribute is null
     *
     * @param method method
     * @param result result
     * @return exception message
     */
    String processor(Method method, Object result);

    default JsonNode getJsonNodeField(Object result, String fieldExpression) throws JsonProcessingException {
        String resultJsonStr = JsonUtils.mapper.writeValueAsString(result);
        JsonNode resultJsonNode = JsonUtils.mapper.readTree(resultJsonStr);
        return getJsonNodeField(resultJsonNode, fieldExpression);
    }

    default JsonNode getJsonNodeField(JsonNode resultJsonNode, String fieldExpression) {
        String[] fields = fieldExpression.split("\\.");
        JsonNode jsonCodeField = null;
        for (String field : fields) {
            jsonCodeField = resultJsonNode.get(field);
        }
        return jsonCodeField;
    }
}
