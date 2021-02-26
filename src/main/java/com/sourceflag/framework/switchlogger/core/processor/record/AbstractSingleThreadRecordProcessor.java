package com.sourceflag.framework.switchlogger.core.processor.record;

import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AbstractSingleThreadRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-02-25 18:49
 * @since 1.0
 */
public abstract class AbstractSingleThreadRecordProcessor implements RecordProcessor {

    private final ExecutorService pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024), new BasicThreadFactory.Builder().namingPattern("switch-logger-record-pool-%d").daemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void processor(InvokeLog invokeLog) throws Throwable {
        pool.execute(() -> {
            try {
                doProcess(invokeLog);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public abstract void doProcess(InvokeLog invokeLog) throws Throwable;
}
