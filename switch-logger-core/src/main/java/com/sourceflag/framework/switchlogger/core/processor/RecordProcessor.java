package com.sourceflag.framework.switchlogger.core.processor;

import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;

public interface RecordProcessor {

    boolean supports(String model) throws UnsupportedModelException;

    void processor(InvokeLog invokeLog) throws Throwable;
}
