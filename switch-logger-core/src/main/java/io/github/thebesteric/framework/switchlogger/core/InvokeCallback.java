package io.github.thebesteric.framework.switchlogger.core;

import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import io.github.thebesteric.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;

public interface InvokeCallback {

    void begin(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper);

    void after(RequestLog requestLog);

}
