package com.sourceflag.framework.switchlogger.redis.configuration;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.SwitchLoggerInitialization;
import com.sourceflag.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import com.sourceflag.framework.switchlogger.redis.record.RedisRecordProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SwitchLoggerRedisInitialization
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-14 23:46
 * @since 1.0
 */
@Slf4j
public class SwitchLoggerRedisInitialization extends SwitchLoggerInitialization {

    public SwitchLoggerRedisInitialization(SwitchLoggerProperties properties, List<SwitchLoggerScanner> switchLoggerScanners) {
        super(properties, switchLoggerScanners);
    }

    @Override
    public void start() {
        String model = properties.getModel();
        int expiredTime = properties.getRedis().getExpiredTime();
        if (SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(model)) {
            if (expiredTime >= 0) {
                ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                        new BasicThreadFactory.Builder().namingPattern("switch-logger-redis-record-schedule-pool-%d").daemon(true).build());
                executorService.scheduleAtFixedRate(getBean(RedisRecordProcessor.class), 0, expiredTime * 1000L / 4, TimeUnit.SECONDS);
            }
        }
        log.info("Switch Logger Redis plugin installed, expired time is {}", expiredTime);
    }
}
