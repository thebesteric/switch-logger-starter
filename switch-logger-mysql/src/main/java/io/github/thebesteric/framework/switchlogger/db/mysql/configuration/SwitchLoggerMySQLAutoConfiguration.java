package io.github.thebesteric.framework.switchlogger.db.mysql.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.exception.ParseErrorException;
import io.github.thebesteric.framework.switchlogger.core.processor.RecordProcessor;
import io.github.thebesteric.framework.switchlogger.db.mysql.configuration.mark.SwitchLoggerDatabaseMarker;
import io.github.thebesteric.framework.switchlogger.db.mysql.record.DatabaseRecordProcessor;
import io.github.thebesteric.framework.switchlogger.db.mysql.utils.SwitchJdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SwitchLoggerRedisAutoConfiguration
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-14 16:28
 * @since 1.0
 */
@Configuration
@Import(SwitchLoggerMySQLInitialization.class)
@EnableConfigurationProperties(SwitchLoggerProperties.class)
public class SwitchLoggerMySQLAutoConfiguration {

    @Resource
    private Environment environment;

    @Bean(name = "switchLoggerDatasource")
    @Conditional(SwitchLoggerDatabaseMarker.class)
    public DruidDataSource dataSource(SwitchLoggerProperties properties) throws ParseErrorException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername((String) checkAndGetEnvironmentVariable(properties.getDatabase().getUsername()));
        dataSource.setPassword((String) checkAndGetEnvironmentVariable(properties.getDatabase().getPassword()));
        dataSource.setDriverClassName((String) checkAndGetEnvironmentVariable(properties.getDatabase().getDriverClassName()));
        dataSource.setUrl((String) checkAndGetEnvironmentVariable(properties.getDatabase().getUrl()));
        dataSource.setMaxActive(Runtime.getRuntime().availableProcessors() * 2 + 1);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(2);
        dataSource.setMaxWait(10000);
        return dataSource;
    }

    @Bean(name = "switchLoggerJdbcTemplate")
    @Conditional(SwitchLoggerDatabaseMarker.class)
    public SwitchJdbcTemplate switchLoggerJdbcTemplate(@Qualifier("switchLoggerDatasource") DruidDataSource dataSource) {
        return new SwitchJdbcTemplate(dataSource);
    }

    @Bean
    @Conditional(SwitchLoggerDatabaseMarker.class)
    public RecordProcessor databaseRecordProcessor(@Qualifier("switchLoggerJdbcTemplate") SwitchJdbcTemplate switchLoggerJdbcTemplate,
                                                   SwitchLoggerProperties properties) {
        return new DatabaseRecordProcessor(switchLoggerJdbcTemplate, properties);
    }

    private static final String regex = "\\$\\{(.*?)\\}";
    private static final Pattern pattern = Pattern.compile(regex);

    private Object checkAndGetEnvironmentVariable(String var) throws ParseErrorException {
        Matcher matcher = pattern.matcher(var);
        while (matcher.find()) {
            String original = matcher.group(0);
            String property = matcher.group(1);
            String[] arr = property.split(":");
            if (arr.length > 2) {
                throw new ParseErrorException(var + " is not the correct expression");
            }
            String value = environment.getProperty(arr[0]);
            if (arr.length == 1) {
                var = var.replace(original, value != null ? value : "");
            } else {
                var = var.replace(original, value != null ? value : arr[1]);
            }
        }
        return var;
    }

}
