package com.sourceflag.framework.switchlogger.core.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * SwitchLoggerRequestWrapper
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-09-29 23:38
 * @since 1.0
 */
public class SwitchLoggerRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public SwitchLoggerRequestWrapper(HttpServletRequest request) {
        super(request);
        byte[] temp = null;
        if (canBeConvert(request)) {
            try (BufferedReader reader = request.getReader()) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                temp = sb.toString().getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        body = temp;
    }

    public String getOriginBody() {
        if (this.body != null) {
            return new String(this.body, StandardCharsets.UTF_8);
        }
        return null;
    }

    public Object getBody() {
        String originBody = getOriginBody();
        if (originBody != null) {
            try {
                return JsonUtils.mapper.readValue(originBody, Map.class);
            } catch (JsonProcessingException e) {
                return originBody;
            }
        }
        return null;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    private boolean canBeConvert(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String contentType = request.getContentType();
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                return !contentType.startsWith("multipart/")
                        && !contentType.startsWith("application/x-www-form-urlencoded")
                        && !contentType.startsWith("application/octet-stream");
            }
        }
        return true;
    }
}
