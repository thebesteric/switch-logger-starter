package io.github.thebesteric.framework.switchlogger.core.processor;

import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.exception.UnsupportedModelException;

public interface RecordProcessor {

    boolean supports(String model) throws UnsupportedModelException;

    void processor(InvokeLog invokeLog) throws Throwable;
}
