package io.github.thebesteric.framework.switchlogger.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class InterfaceMetricRequestLog extends RequestLog {

    private InterfaceMetric metric;

    public InterfaceMetricRequestLog(RequestLog requestLog, InterfaceMetric metric) {
        BeanUtils.copyProperties(requestLog, this);
        this.metric = metric;
    }

    @Getter
    @Setter
    public static class InterfaceMetric {

        @JsonProperty(value = "total_request")
        private long totalRequest = 0L;
        @JsonProperty(value = "avg_response_time")
        private long avgResponseTime = 0L;
        @JsonProperty(value = "min_response_time")
        private long minResponseTime = 0L;
        @JsonProperty(value = "max_response_time")
        private long maxResponseTime = 0L;
        @JsonIgnore
        private long totalResponseTime = 0L;

        public synchronized void calc(Long duration) {
            totalRequest++;
            totalResponseTime += duration;
            avgResponseTime = totalResponseTime / totalRequest;
            if (duration > maxResponseTime) {
                maxResponseTime = duration;
            }
            if (duration < minResponseTime || minResponseTime == 0L) {
                minResponseTime = duration;
            }
        }
    }

}