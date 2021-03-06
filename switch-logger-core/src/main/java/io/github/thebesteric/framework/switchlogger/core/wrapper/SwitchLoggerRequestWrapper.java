package io.github.thebesteric.framework.switchlogger.core.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
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
        byte[] temp = new byte[0];
        if (canBeConvert(request)) {
            temp = getRequestBody(request);
        }
        body = temp;
    }


    private byte[] getRequestBody(HttpServletRequest request) {
        byte[] buffer = new byte[0];
        InputStream in = null;
        int len = request.getContentLength();
        if (len > 0) {
            try {
                buffer = new byte[len];
                in = request.getInputStream();
                int read = 1;
                int totalRead = 0;
                while (read > 0) {
                    read = in.read(buffer, totalRead, buffer.length - totalRead);
                    if (read > 0)
                        totalRead += read;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return buffer;
    }

    public String getRawBody() {
        if (this.body != null && this.body.length > 0) {
            return new String(this.body, StandardCharsets.UTF_8);
        }
        return null;
    }

    public Object getBody() {
        String rawBody = getRawBody();
        if (rawBody != null) {
            try {
                return JsonUtils.mapper.readValue(rawBody, Map.class);
            } catch (JsonProcessingException e) {
                return rawBody;
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
        String contentType = request.getContentType();
        if (contentType != null) {
            contentType = contentType.toLowerCase();
            return !contentType.startsWith("multipart/")
                    && !contentType.startsWith("application/x-www-form-urlencoded")
                    && !contentType.startsWith("application/octet-stream");
        }
        return true;
    }
}
