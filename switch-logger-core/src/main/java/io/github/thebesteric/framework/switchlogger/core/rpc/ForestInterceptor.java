package io.github.thebesteric.framework.switchlogger.core.rpc;

import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.Interceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import io.github.thebesteric.framework.switchlogger.utils.TransactionUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class ForestInterceptor<T> extends RpcHandler implements Interceptor<T> {

    private final static ThreadLocal<RequestLog> requestLogThreadLocal = new ThreadLocal<>();

    @Override
    public boolean beforeExecute(ForestRequest request) {
        if (!properties.getRpc().getForest().isEnable() || request == null) {
            return Interceptor.super.beforeExecute(request);
        }
        long startTime = System.currentTimeMillis();

        // basic
        RequestLog requestLog = new RequestLog();
        requestLog.setCreatedTime(System.currentTimeMillis());
        requestLog.setTag(RequestLog.FOREST_TAG);
        requestLog.setLevel(RequestLog.LEVEL_INFO);
        requestLog.setMethod(request.getType().getName().toUpperCase(Locale.ROOT));
        requestLog.setTrackId(TransactionUtils.get());
        requestLog.setRequestSessionId(TransactionUtils.get());

        // uri & url
        URI uri = request.getURI();
        requestLog.setUri(uri.getPath());
        requestLog.setProtocol(request.getProtocol().getName().toUpperCase(Locale.ROOT));
        requestLog.setServerName(uri.getHost());
        requestLog.setRemoteAddr(uri.getHost());
        requestLog.setRemotePort(uri.getPort());
        requestLog.setUrl(request.getUrl());

        // request body
        try {
            Map<String, Object> nameValuesMap = request.body().nameValuesMap();
            if (nameValuesMap != null) {
                String rawBody = JsonUtils.mapper.writeValueAsString(nameValuesMap);
                requestLog.setRawBody(rawBody);
                requestLog.setBody(JsonUtils.mapper.readValue(requestLog.getRawBody(), Map.class));
            }
        } catch (JsonProcessingException e) {
            log.warn(String.format("Cannot parse body to json: %s", requestLog.getRawBody()));
        }

        // request params
        StringBuilder queryStringBuilder = new StringBuilder();
        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            request.getQuery().forEach((k, v) -> {
                queryStringBuilder.append(k).append("=").append(v).append("&");
            });
            String queryString = queryStringBuilder.toString();
            requestLog.setQueryString(queryString.substring(0, queryStringBuilder.length() - 1));
        }

        // TODO request headers: wait for forest framework update

        // method
        InvokeLog.ExecuteInfo executeInfo = new InvokeLog.ExecuteInfo();
        executeInfo.setStartTime(startTime);

        Method method = request.getMethod().getMethod();
        executeInfo.setClassName(method.getDeclaringClass().getName());

        InvokeLog.ExecuteInfo.MethodInfo methodInfo = new InvokeLog.ExecuteInfo.MethodInfo();
        methodInfo.setMethodName(method.getName());
        methodInfo.setReturnType(method.getReturnType().getName());

        // method signatures
        final LinkedHashMap<String, Object> methodSignatures = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            methodSignatures.put(parameter.getName(), parameter.getParameterizedType().getTypeName());
        }
        methodInfo.setSignatures(methodSignatures);

        // TODO method args


        // set MethodInfo
        executeInfo.setMethodInfo(methodInfo);

        // set ExecuteInfo
        requestLog.setExecuteInfo(executeInfo);

        requestLogThreadLocal.set(requestLog);
        return true;
    }

    @SneakyThrows
    @Override
    public void onSuccess(T data, ForestRequest request, ForestResponse response) {
        final RequestLog requestLog = requestLogThreadLocal.get();

        // duration
        requestLog.setDuration(System.currentTimeMillis() - requestLog.getExecuteInfo().getStartTime());

        // status & level
        int status = response.getStatusCode();
        if (!properties.getRpc().getFeign().getSucceedCodes().contains(status)) {
            requestLog.setLevel(RequestLog.LEVEL_ERROR);
        }

        // response info
        RequestLog.Response logResponse = new RequestLog.Response();
        logResponse.setStatus(status);
        Map<String, String> headers = new HashMap<>();
        response.getHeaders().forEach((key, values) -> {
            String value = String.join(",", values);
            headers.put(key, value);
        });
        logResponse.setHeaders(headers);
        requestLog.setResponse(logResponse);

        // result info
        requestLog.setResult(data);

        recordRequestLog(requestLog);

    }

    @Override
    public void onError(ForestRuntimeException ex, ForestRequest request, ForestResponse response) {
        final RequestLog requestLog = requestLogThreadLocal.get();
        requestLog.setLevel(RequestLog.LEVEL_ERROR);
        requestLog.setException(ex.getMessage());
        requestLog.setDuration(System.currentTimeMillis() - requestLog.getExecuteInfo().getStartTime());
    }

    @SneakyThrows
    @Override
    public void afterExecute(ForestRequest request, ForestResponse response) {
        requestLogThreadLocal.remove();
    }
}
