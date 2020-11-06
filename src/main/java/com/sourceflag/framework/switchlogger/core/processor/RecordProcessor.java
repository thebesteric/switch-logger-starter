package com.sourceflag.framework.switchlogger.core.processor;

import com.sourceflag.framework.switchlogger.core.RequestLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;

public interface RecordProcessor {

    boolean supports(String model) throws UnsupportedModelException;

    void processor(RequestLog requestLog) throws Exception;
}
