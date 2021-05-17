package com.sourceflag.framework.switchlogger.core;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * SwitchLoggerInitialization
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-01 00:28
 * @since 1.0
 */
@Slf4j
public class SwitchLoggerCoreInitialization extends SwitchLoggerInitialization {

    public SwitchLoggerCoreInitialization(SwitchLoggerProperties properties, List<SwitchLoggerScanner> switchLoggerScanners) {
        super(properties, switchLoggerScanners);
    }

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

        log.info("Switch Logger Record Model is {}, Running Model is {}", properties.getModel(), properties.isAsync() ? "Async" : "Sync");

        String projectPath = getProjectPath();
        // scanner @Controller and @SwitchLogger and so on
        for (SwitchLoggerScanner switchLoggerScanner : switchLoggerScanners) {
            switchLoggerScanner.doScan(new File(projectPath + "/"), properties.getCompilePath());
        }

        // print url_mapping to console
        if (log.isTraceEnabled()) {
            log.info("Switch Logger Project Path is {}", projectPath);
            SwitchLoggerFilterWrapper.URL_MAPPING.forEach((k, v) -> log.info("Switch Logger Scan {} => {}", k, v.getName()));
        }
    }
}