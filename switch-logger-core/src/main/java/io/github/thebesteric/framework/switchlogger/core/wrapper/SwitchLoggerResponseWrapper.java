package io.github.thebesteric.framework.switchlogger.core.wrapper;

import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * SwitchLoggerResponseWrapper
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-09-29 23:19
 * @since 1.0
 */
public class SwitchLoggerResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer;
    private final ServletOutputStream out;

    @Getter
    @Setter
    private String level = InvokeLog.LEVEL_INFO;

    @Getter
    @Setter
    private Exception exception;

    public SwitchLoggerResponseWrapper(HttpServletResponse response) {
        super(response);
        buffer = new ByteArrayOutputStream();
        out = new WrapperOutputStream(buffer);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (out != null) out.flush();
    }

    public void setBuffer(String message) throws IOException {
        buffer.reset();
        buffer.write(message.getBytes());
    }

    public byte[] getByteArray() throws IOException {
        flushBuffer();
        return buffer.toByteArray();
    }

    private static class WrapperOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream byteArrayOutputStream;

        public WrapperOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }
}
