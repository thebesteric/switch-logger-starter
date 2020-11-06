package com.sourceflag.framework.switchlogger.core.wrapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public SwitchLoggerRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            body = sb.toString().getBytes();
        }
    }

    public Object getBody() {
        if (this.body != null) {
            String content = new String(this.body, StandardCharsets.UTF_8);
            try {
                return JsonUtils.mapper.readValue(content, Map.class);
            } catch (JsonProcessingException e) {
                return content;
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
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }
}
