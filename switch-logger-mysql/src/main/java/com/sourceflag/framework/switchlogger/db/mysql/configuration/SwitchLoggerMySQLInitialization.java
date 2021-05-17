package com.sourceflag.framework.switchlogger.db.mysql.configuration;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.SwitchLoggerInitialization;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.domain.RequestLog;
import com.sourceflag.framework.switchlogger.core.exception.UnsupportedModelException;
import com.sourceflag.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import com.sourceflag.framework.switchlogger.db.mysql.utils.SwitchJdbcTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * SwitchLoggerRedisInitialization
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-14 23:46
 * @since 1.0
 */
@Slf4j
public class SwitchLoggerMySQLInitialization extends SwitchLoggerInitialization {

    public SwitchLoggerMySQLInitialization(SwitchLoggerProperties properties, List<SwitchLoggerScanner> switchLoggerScanners) {
        super(properties, switchLoggerScanners);
    }

    @SneakyThrows
    @Override
    public void start() {
        String model = properties.getModel();
        if (SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(model)) {
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
        log.info("Switch Logger MySQL plugin installed");
    }
}
