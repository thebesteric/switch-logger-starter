package io.github.thebesteric.framework.switchlogger.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

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

    // whether private methods are logged
    private boolean privateMethodLogging = false;

    // if global exception handling is used
    private GlobalSuccessResponse globalSuccessResponse;

    // decide whether to use a thread pool
    private boolean async = false;

    // support maven and gradle
    private String[] compilePath = {"target\\classes", "build\\classes\\java\\main"};

    // LOG, STDOUT, CACHE, REDIS, ES, DATABASE
    private String model = ModelType.LOG.name();

    private Redis redis = new Redis();

    private Database database = new Database();

    private Cache cache = new Cache();

    private Filter filter = new Filter();

    // decide whether to use SkyWalking trace id
    private boolean skyWalkingTrace = false;

    // RPC Config
    private Rpc rpc = new Rpc();

    @Data
    public static class GlobalSuccessResponse {
        private boolean useDefault;
        private List<ResponseEntity> responseEntities;
        private List<String> messageFields;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ResponseEntity {
            private String codeField;
            private String codeValue;
        }
    }

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

    @Data
    public static class Rpc {

        private Feign feign = new Feign();
        private Forest forest = new Forest();

        @Data
        public static class Feign {
            public static final int DEFAULT_SUCCEED_CODE = 200;
            private boolean enable = true;
            private List<Integer> succeedCodes = Collections.singletonList(DEFAULT_SUCCEED_CODE);
        }

        @Data
        public static class Forest {
            public static final int DEFAULT_SUCCEED_CODE = 200;
            private boolean enable = false;
            private List<Integer> succeedCodes = Collections.singletonList(DEFAULT_SUCCEED_CODE);
        }
    }

}
