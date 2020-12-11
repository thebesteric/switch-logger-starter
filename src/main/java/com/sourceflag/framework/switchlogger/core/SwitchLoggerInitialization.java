package com.sourceflag.framework.switchlogger.core;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.domain.RequestLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;
import com.sourceflag.framework.switchlogger.core.processor.record.RedisRecordProcessor;
import com.sourceflag.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SwitchLoggerInitialization
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 00:28
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SwitchLoggerInitialization implements SmartLifecycle, ApplicationContextAware {

    private boolean isRunning = false;

    private final SwitchLoggerProperties properties;

    private final List<SwitchLoggerScanner> switchLoggerScanners;

    private GenericApplicationContext applicationContext;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @SneakyThrows
    @Override
    public void start() {
        if (!properties.isEnable()) {
            log.info("SWITCH LOGGER is disabled");
            return;
        }

        String model = properties.getModel();
        if (SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(model)) {
            int expiredTime = properties.getRedis().getExpiredTime();
            if (expiredTime >= 0) {
                ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                        new BasicThreadFactory.Builder().namingPattern("redis-record-schedule-pool-%d").daemon(true).build());
                executorService.scheduleAtFixedRate(getBean(RedisRecordProcessor.class), 0, expiredTime * 1000 / 4, TimeUnit.SECONDS);
            }
        } else if (SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(model)) {
            if ("mysql".equalsIgnoreCase(properties.getDatabase().getType())) {
                try {
                    SwitchJdbcTemplate jdbcTemplate = getBean(SwitchJdbcTemplate.class);
                    String tableNamePrefix = properties.getDatabase().getTableName();
                    jdbcTemplate.createTable(tableNamePrefix, RequestLog.class);
                    jdbcTemplate.createTable(tableNamePrefix, InvokeLog.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new UnsupportedModelException(properties.getDatabase().getType());
            }
        }

        String projectPath = getProjectPath();
        log.info("PROJECT_PATH is {}, RECORD_MODEL is {}", projectPath, model.toUpperCase());

        // scanner @Controller and @SwitchLogger and so on
        for (SwitchLoggerScanner switchLoggerScanner : switchLoggerScanners) {
            switchLoggerScanner.doScan(new File(projectPath + "/"), properties.getCompilePath());
        }

        // print url_mapping to console
        if (log.isTraceEnabled()) {
            SwitchLoggerFilterWrapper.URL_MAPPING.forEach((k, v) -> log.info("SWITCH LOGGER SCAN {} => {}", k, v.getName()));
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    public String getProjectPath() {
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        if (defaultClassLoader != null) {
            String path = Objects.requireNonNull(defaultClassLoader.getResource("")).getPath();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                path = path.substring(1);
            }
            return path.replaceAll("%20", " ").replace("target/classes/", "");
        }
        return null;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }

    private <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
