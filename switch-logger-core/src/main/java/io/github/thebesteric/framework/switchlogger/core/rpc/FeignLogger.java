package io.github.thebesteric.framework.switchlogger.core.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.Request;
import feign.Response;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import io.github.thebesteric.framework.switchlogger.utils.TransactionUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class FeignLogger extends RpcHandler {

    private final static ThreadLocal<RequestLog> requestLogThreadLocal = new ThreadLocal<>();

    @Override
    protected void log(String configKey, String format, Object... args) {
        log.info(String.format(methodTag(configKey) + format, args));
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (!properties.getRpc().getFeign().isEnable() || request.requestTemplate() == null) {
            super.logRequest(configKey, logLevel, request);
            return;
        }
        long startTime = System.currentTimeMillis();

        // basic
        RequestLog requestLog = new RequestLog();
        requestLog.setCreatedTime(System.currentTimeMillis());
        requestLog.setTag(RequestLog.FEIGN_TAG);
        requestLog.setLevel(RequestLog.LEVEL_INFO);
        requestLog.setMethod(request.httpMethod().name().toUpperCase(Locale.ROOT));
        requestLog.setTrackId(TransactionUtils.get());
        requestLog.setRequestSessionId(TransactionUtils.get());

        // uri & url
        String url = request.requestTemplate().url();
        requestLog.setUrl(url);
        String[] arr = url.split("//");
        if (arr.length > 1) {
            String uri = arr[1].substring(arr[1].indexOf("/"));
            requestLog.setUri(uri);
        }

        // uri info
        URI uri = URI.create(url);
        requestLog.setProtocol(uri.getScheme());
        requestLog.setServerName(uri.getHost());
        requestLog.setRemoteAddr(uri.getHost());
        requestLog.setRemotePort(uri.getPort());
        requestLog.setQueryString(uri.getQuery());

        // request body
        requestLog.setRawBody(request.body() == null ? null : new String(request.body(), StandardCharsets.UTF_8));
        if (requestLog.getRawBody() != null) {
            try {
                requestLog.setBody(JsonUtils.mapper.readValue(requestLog.getRawBody(), Map.class));
            } catch (JsonProcessingException e) {
                log.warn(String.format("Cannot parse body to json: %s", requestLog.getRawBody()));
            }
        }

        // url params
        final HashMap<String, String> params = new LinkedHashMap<>();
        if (requestLog.getUrl() != null && requestLog.getUrl().split("\\?").length > 1) {
            final String[] urlSplitArr = requestLog.getUrl().split("\\?");
            final String[] paramKeyValueArr = urlSplitArr[1].split("&");
            for (String keyValue : paramKeyValueArr) {
                final String[] keyValueArr = keyValue.split("=");
                params.put(keyValueArr[0], keyValueArr.length > 1 ? keyValueArr[1] : null);
            }
        }
        requestLog.setParams(params);

        // headers
        Map<String, String> headers = new HashMap<>();
        request.requestTemplate().headers().forEach((key, values) -> {
            String value = String.join(",", values);
            headers.put(key, value);
        });
        requestLog.setHeaders(headers);

        // execute info
        InvokeLog.ExecuteInfo executeInfo = new InvokeLog.ExecuteInfo();
        executeInfo.setStartTime(startTime);

        // class info
        if (request.requestTemplate().feignTarget() != null && request.requestTemplate().feignTarget().type() != null) {
            Class<?> clazz = request.requestTemplate().feignTarget().type();
            executeInfo.setClassName(clazz.getName());
        }

        // method info
        if (request.requestTemplate().methodMetadata() != null && request.requestTemplate().methodMetadata().returnType() != null) {
            InvokeLog.ExecuteInfo.MethodInfo methodInfo = new InvokeLog.ExecuteInfo.MethodInfo();
            Method method = request.requestTemplate().methodMetadata().method();
            methodInfo.setMethodName(method.getName());
            methodInfo.setReturnType(method.getReturnType().getName());

            // method signatures
            final LinkedHashMap<String, Object> methodSignatures = new LinkedHashMap<>();
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                methodSignatures.put(parameter.getName(), parameter.getParameterizedType().getTypeName());
            }
            methodInfo.setSignatures(methodSignatures);

            // method arguments
            final LinkedHashMap<String, Object> methodArgs = new LinkedHashMap<>();
            if (request.requestTemplate().methodMetadata().indexToName() != null && request.requestTemplate().methodMetadata().indexToName().size() > 0) {
                for (Map.Entry<Integer, Collection<String>> entry : request.requestTemplate().methodMetadata().indexToName().entrySet()) {
                    final String paramName = entry.getValue().toArray()[0].toString();
                    final Collection<String> paramValues = request.headers().getOrDefault(paramName, null);
                    String paramValue = null;
                    if (paramValues != null && paramValues.size() > 0) {
                        paramValue = paramValues.toArray()[0].toString();
                    }
                    if (paramValue == null && params.containsKey(paramName)) {
                        paramValue = params.get(paramName);
                    }
                    methodArgs.put(paramName, paramValue);
                }
                methodInfo.setArguments(methodArgs);
            }

            // set MethodInfo
            executeInfo.setMethodInfo(methodInfo);
        }

        // set ExecuteInfo
        requestLog.setExecuteInfo(executeInfo);

        requestLogThreadLocal.set(requestLog);
    }

    @SneakyThrows
    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {

        final RequestLog requestLog = requestLogThreadLocal.get();
        requestLogThreadLocal.remove();

        // duration
        requestLog.setDuration(elapsedTime);

        // status & level
        int status = response.status();
        if (!RpcHandler.properties.getRpc().getFeign().getSucceedCodes().contains(status)) {
            requestLog.setLevel(RequestLog.LEVEL_ERROR);
        }

        // response info
        RequestLog.Response logResponse = new RequestLog.Response();
        logResponse.setStatus(status);
        Map<String, String> headers = new HashMap<>();
        response.headers().forEach((key, values) -> {
            String value = String.join(",", values);
            headers.put(key, value);
        });
        logResponse.setHeaders(headers);
        requestLog.setResponse(logResponse);

        // result info
        if (response.body() != null) {
            byte[] bodyData = feign.Util.toByteArray(response.body().asInputStream());
            if (bodyData.length > 0) {
                String responseBody = feign.Util.decodeOrDefault(bodyData, feign.Util.UTF_8, "Binary data");
                try {
                    requestLog.setResult(JsonUtils.mapper.readTree(responseBody));
                } catch (Exception exception) {
                    requestLog.setResult(responseBody);
                }
            }

            recordRequestLog(requestLog);
            return response.toBuilder().body(bodyData).build();
        }

        recordRequestLog(requestLog);

        return response;
    }

    @SneakyThrows
    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        final RequestLog requestLog = requestLogThreadLocal.get();
        requestLogThreadLocal.remove();
        requestLog.setLevel(RequestLog.LEVEL_ERROR);
        requestLog.setException(ioe.getMessage());
        requestLog.setDuration(elapsedTime);
        recordRequestLog(requestLog);
        return ioe;
    }


}
