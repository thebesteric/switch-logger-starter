package com.sourceflag.framework.switchlogger.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sourceflag.framework.switchlogger.annotation.Column;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * RequestLog
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-09-30 14:08
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class RequestLog extends AbstractEntity {

    public RequestLog(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper) throws IOException {

        // timestamp
        this.timestamp = System.currentTimeMillis();

        // trackId
        this.trackId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        // params
        Enumeration<String> parameterNames = requestWrapper.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameterValue = requestWrapper.getParameter(parameterName);
            this.getParams().put(parameterName, parameterValue);
        }

        // headers
        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = requestWrapper.getHeader(headerName);
            this.getHeaders().put(headerName, headerValue);
        }

        // cookies
        javax.servlet.http.Cookie[] cookies = requestWrapper.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                this.getCookies().add(new RequestLog.Cookie(cookie));
            }
        }

        // body
        Object body = requestWrapper.getBody();
        this.setBody(body);

        // result
        byte[] bytes = responseWrapper.getByteArray();
        if (bytes != null) {
            this.result = new String(bytes, StandardCharsets.UTF_8);
        }

        // serverName
        this.serverName = requestWrapper.getServerName();

        // requestSessionId
        this.requestSessionId = requestWrapper.getRequestedSessionId();

        // queryString
        this.queryString = requestWrapper.getQueryString();

        // method
        this.method = requestWrapper.getMethod();

        // protocol
        this.protocol = requestWrapper.getProtocol();

        // remoteAddr
        this.remoteAddr = requestWrapper.getRemoteAddr();

        // remotePort
        this.remotePort = requestWrapper.getRemotePort();

        // url
        this.url = requestWrapper.getRequestURL().toString();

        // uri
        this.uri = requestWrapper.getRequestURI();
    }


    @JsonProperty("track_id")
    @Column(length = 64)
    private String trackId;

    @JsonProperty("request_session_id")
    @Column(length = 64)
    private String requestSessionId;

    @Column(length = 512)
    private String uri;

    @Column(length = 512)
    private String url;

    @Column(length = 128)
    private String method;

    @Column(length = 64)
    private String protocol;

    @JsonProperty("server_name")
    @Column(length = 128)
    private String serverName;

    @JsonProperty("remote_addr")
    @Column(length = 64)
    private String remoteAddr;

    @JsonProperty("remote_port")
    @Column(length = 11, type = "int")
    private int remotePort;

    @JsonProperty("query_string")
    @Column(length = 2048)
    private String queryString;

    @Column(length = 20, type = "bigint")
    private long timestamp;

    @Column(type = "json")
    private Set<Cookie> cookies = new HashSet<>();

    @Column(type = "json")
    private Map<String, String> headers = new HashMap<>();

    @Column(type = "json")
    private Map<String, String> params = new HashMap<>();

    @Column(type = "json")
    private Object body;

    @Column(type = "json")
    private Object result;

    @JsonProperty("execute_info")
    @Column(type = "json")
    private ExecuteInfo executeInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExecuteInfo extends AbstractEntity {

        public ExecuteInfo(Method method, long startTime, long duration) {
            this.className = method.getDeclaringClass().getName();
            this.methodInfo = new MethodInfo(method);
            this.startTime = startTime;
            this.duration = duration;
        }

        private String className;

        private MethodInfo methodInfo;

        @JsonProperty("start_time")
        private long startTime;

        private long duration;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class MethodInfo extends AbstractEntity {

            private String methodName;
            private LinkedHashMap<String, Object> signatures = new LinkedHashMap<>();
            private String returnType;

            public MethodInfo(Method method) {
                this.methodName = method.getName();
                Parameter[] params = method.getParameters();
                if (params != null && params.length > 0) {
                    for (Parameter param : params) {
                        signatures.put(param.getName(), param.getParameterizedType().getTypeName());
                    }
                }
                this.returnType = method.getReturnType().getName();
            }

        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Cookie extends AbstractEntity {

        public Cookie(javax.servlet.http.Cookie cookie) {
            this.name = cookie.getName();
            this.value = cookie.getValue();
            this.comment = cookie.getComment();
            this.domain = cookie.getDomain();
            this.maxAge = cookie.getMaxAge();
            this.path = cookie.getPath();
            this.secure = cookie.getSecure();
            this.version = cookie.getVersion();
            this.isHttpOnly = cookie.isHttpOnly();
        }

        private String name;

        private String value;

        private String comment;

        private String domain;

        private int maxAge = -1;

        private String path;

        private boolean secure;

        private int version = 0;

        private boolean isHttpOnly = false;

    }

}
