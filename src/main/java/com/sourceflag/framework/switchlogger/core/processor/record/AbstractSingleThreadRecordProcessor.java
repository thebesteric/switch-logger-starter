package com.sourceflag.framework.switchlogger.core.processor.record;

import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AbstractSingleThreadRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-02-25 18:49
 * @since 1.0
 */
public abstract class AbstractSingleThreadRecordProcessor implements RecordProcessor {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void processor(InvokeLog invokeLog) throws Throwable {
        executorService.execute(() -> {
            try {
                doProcess(invokeLog);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public abstract void doProcess(InvokeLog invokeLog) throws Throwable;
}
