package io.github.thebesteric.framework.switchlogger.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.switchlogger.annotation.Column;
import io.github.thebesteric.framework.switchlogger.annotation.Table;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;
import lombok.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
@Table(name = "request")
public class RequestLog extends InvokeLog {

    public RequestLog(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper, ThreadLocal<String> trackIdThreadLocal, long duration) throws IOException {

        // type
        this.level = responseWrapper.getLevel();

        // exception
        this.exception = responseWrapper.getException() != null ? responseWrapper.getException().getMessage() : null;

        // createdTime
        this.createdTime = System.currentTimeMillis();

        // trackId
        this.trackId = trackIdThreadLocal.get();

        // duration
        this.duration = duration;

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
        this.body = requestWrapper.getBody();

        // originBody
        this.rawBody = requestWrapper.getRawBody();

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

        // response
        this.response = new Response();
        response.setStatus(responseWrapper.getStatus());
        response.setContentType(responseWrapper.getContentType());
        response.setLocale(responseWrapper.getLocale().toString());
        Map<String, String> responseHeaders = responseWrapper.getHeaderNames().stream().collect(Collectors.toMap((key) -> key, responseWrapper::getHeader));
        response.setHeaders(responseHeaders);
    }

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

    @Column(type = "json")
    private Set<Cookie> cookies = new HashSet<>();

    @Column(type = "json")
    private Map<String, String> headers = new HashMap<>();

    @Column(type = "json")
    private Map<String, String> params = new HashMap<>();

    @Column(type = "json")
    private Object body;

    @JsonProperty("raw_body")
    @Column(length = 2048)
    private String rawBody;

    @JsonProperty("duration")
    @Column(length = 11, type = "int")
    private long duration;

    @Column(type = "json")
    private Response response;

    @Data
    @EqualsAndHashCode(callSuper = true)
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

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class Response extends AbstractEntity {
        private int status;
        private String contentType;
        private String locale;
        private Map<String, String> headers = new HashMap<>();
    }

}
