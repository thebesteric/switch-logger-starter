package io.github.thebesteric.framework.switchlogger.core.processor.logger;

import io.github.thebesteric.framework.switchlogger.core.domain.InterfaceMetricRequestLog;
import io.github.thebesteric.framework.switchlogger.core.domain.RequestLog;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InterfaceMetricRequestLoggerProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-03-13 00:08
 * @since 1.0
 */
@Slf4j
public class InterfaceMetricRequestLoggerProcessor extends AbstractRequestLoggerProcessor {

    public static final Map<String, InterfaceMetricRequestLog.InterfaceMetric> METRIC_MAP = new ConcurrentHashMap<>(256);

    @Override
    public RequestLog doAfterProcessor(RequestLog requestLog) {
        InterfaceMetricRequestLog.InterfaceMetric metric = METRIC_MAP.get(requestLog.getUri());
        if (metric == null) {
            metric = new InterfaceMetricRequestLog.InterfaceMetric();
            METRIC_MAP.put(requestLog.getUri(), metric);
        }
        metric.calc(requestLog.getDuration());
        return new InterfaceMetricRequestLog(requestLog, metric);
    }

}
