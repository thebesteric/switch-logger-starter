package com.sourceflag.framework.switchlogger.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SwitchLoggerProperties
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-12 00:29
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = SwitchLoggerProperties.PROPERTIES_PREFIX)
public class SwitchLoggerProperties {

    public static final String PROPERTIES_PREFIX = "sourceflag.switch-logger";

    public enum ModelType {
        LOG, STDOUT, CACHE, REDIS, ES, DATABASE
    }

    private boolean enable = true;

    private String[] compilePath = {"target\\classes", "build\\classes\\java\\main"};

    private String model = ModelType.LOG.name();

    private Redis redis = new Redis();

    private Database database = new Database();

    private Cache cache = new Cache();

    private Filter filter = new Filter();

    @Data
    public static class Filter {
        private String name = "SwitchLoggerFilter";
        private int order = 1;
        private String[] urlPatterns = {"/*"};
        private String[] include = {".*"};
        private String[] exclude = {};
    }

    @Data
    public static class Redis {
        private String key = "SWITCH_LOGGER";
        private int database = 1;
        private int expiredTime = 3600;
    }

    @Data
    public static class Database {
        private String type = "mysql";
        private String tableName = "switch_logger";
        private String url = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&userSSL=false&serverTimezone=Asia/Shanghai";
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
        private String username = "root";
        private String password = "root";
    }

    @Data
    public static class Cache {
        private int initialCapacity = 2000;
        private int maximumSize = 20000;
        private int expiredTime = 3600;
    }

}
