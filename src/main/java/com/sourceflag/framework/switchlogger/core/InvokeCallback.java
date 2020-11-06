package com.sourceflag.framework.switchlogger.core;

import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;

public interface InvokeCallback {

    void begin(SwitchLoggerRequestWrapper requestWrapper, SwitchLoggerResponseWrapper responseWrapper);

    void after(RequestLog requestLog);

}
