package io.github.thebesteric.framework.switchlogger.core.processor.record;

import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.processor.RecordProcessor;
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

    protected final SwitchLoggerProperties properties;

    private ExecutorService pool;

    public AbstractSingleThreadRecordProcessor(SwitchLoggerProperties properties) {
        this.properties = properties;
        if (properties.isAsync()) {
            this.pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024), new BasicThreadFactory.Builder().namingPattern("switch-logger-record-pool-%d").daemon(true).build(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    @Override
    public void processor(InvokeLog invokeLog) throws Throwable {
        if (pool != null) {
            pool.execute(() -> {
                doExecute(invokeLog);
            });
        } else {
            doExecute(invokeLog);
        }
    }

    private void doExecute(InvokeLog invokeLog) {
        try {
            doProcess(invokeLog);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public abstract void doProcess(InvokeLog invokeLog) throws Throwable;
}
