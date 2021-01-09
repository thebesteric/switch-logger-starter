## Global log Framework

> 基于 Spring Boot 的一款全局控制层日志记录插件

### Quick start
在启动类注解上标记 `@EnableSwitchLogger` 即可
```java
@EnableSwitchLogger
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
添加上 `@EnableSwitchLogger` 后，即可对所有 Controller 层按照默认规则进行全局控制层日志记录

### 配置
```yaml
sourceflag.switch-logger:
    enable: true # available or not, default is true
    model: log # logging mode, support log, stdout, mysql, redis, local cache
```
- LOG 配置模式
```yaml
sourceflag.switch-logger:
    model: log
```
- STDOUT 配置模式
```yaml
sourceflag.switch-logger:
    model: log
```
- MySQL 配置模式
```yaml
sourceflag.switch-logger:
    model: database
    database:
      tableName: SWITCH_LOGGER # table prefix
      url: "jdbc:mysql://localhost:3306/test"
      driverClassName: "com.mysql.cj.jdbc.Driver"
      username: "root"
      password: "root"
```
- Cache 配置模式
```yaml
sourceflag.switch-logger:
    model: cache
    cache:
      initial-capacity: 2000
      maximum-size: 20000
      expired-time: 3600
```
- Redis 配置模式
```yaml
sourceflag.switch-logger:
    model: redis
    redis:
      key: SWITCH_LOGGER
      database: 0
      expired-time: 3600
```
- filter 自定义拦截位置
```yaml
sourceflag.switch-logger:
    model: log
    filter:
      url-patterns: ["/*"]
      include: [".*"]
      exclude: []
```

### 扩展
- MappingProcessor 接口: 可自定义接口扫描规则
```java
@Component
public class RequestMappingProcessor implements MappingProcessor {

    private Method method;

    @Override
    public boolean supports(Method method) {
        this.method = method;
        return method.getAnnotation(RequestMapping.class) != null;
    }

    @Override
    public void processor(String[] classRequestMappingUrls) {
        if (classRequestMappingUrls != null && classRequestMappingUrls.length > 0) {
            for (String classRequestMappingUrl : classRequestMappingUrls) {
                String[] methodRequestMappingUrls = method.getAnnotation(RequestMapping.class).value();
                doProcessor(methodRequestMappingUrls, classRequestMappingUrl, method);
            }
        }
    }
}
```

- RecordProcessor 接口: 可自定义日志扫描规则
```java
@Slf4j
@Component
public class LogRecordProcessor implements RecordProcessor {
    @Override
    public boolean supports(String model) {
        return model == null || model.trim().equals("") || SwitchLoggerProperties.ModelType.LOG.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(RequestLog requestLog) throws JsonProcessingException {
        log.info(JsonUtils.mapper.writeValueAsString(requestLog));
    }
}
```

- IgnoreUrlProcessor 接口: 可自定义 URL 忽略规则
```java
@EnableSwitchLogger
@Configuration
public class SwitchLoggerConfiguration {
    @Bean
    public IgnoreUrlProcessor ignoreUrlProcessor() {
        return ignoreUrls -> {
            ignoreUrls.add("/swagger-ui");
            ignoreUrls.add("/swagger-resources");
            ignoreUrls.add("/v2/api-docs");
        };
    }
}
```

- RequestLoggerProcessor 接口: 可自定义日志处理规则（暂不支持数据库模式）
```java
@EnableSwitchLogger
@Configuration
public class SwitchLoggerConfiguration {
    @Bean
    public RequestLoggerProcessor requestLoggerProcessor() {
        return new RequestLoggerProcessor() {
            @Override
            public RequestLog processor(SwitchLoggerRequestWrapper switchLoggerRequestWrapper, SwitchLoggerResponseWrapper switchLoggerResponseWrapper, Map<String, Method> map, long l) throws IOException {
                RequestLog requestLog = new RequestLog();
                requestLog.setBody("Let me control");
                return requestLog;
            }
        };
    }
}
``` 

- AttributeProcessor 接口: 自定义注入属性解析器
```java
@EnableSwitchLogger
@Configuration
public class SwitchLoggerConfiguration {
    @Bean
    public AttributeProcessor attributeProcessor(){
        return new AttributeProcessor() {
            @Override
            public boolean supports(Field sourceField) {
                return false;
            }

            @Override
            public void processor(Field targetField, Object bean) throws Throwable {
                // some logic
            }
        };
    }
}
```

### @SwitchLogger 使用
@SwitchLogger 可作用于类或方法上，用于记录非 @Controller 层的方法执行过程，同时该注解会记录 trackId，作为 Controller 层的调用链关系
> 默认含有 @Controller 或 @RestController 层无需添加 @SwitchLogger 注解，框架会自动应用
```java
@Component
@SwitchLogger(tag = "service", extra = {"hello", "world"})
public class UserService {
    @SwitchLogger(tag = "foo", extra = {"foo", "bar"})
    public List<String> foo(List<String> arr) {
        arr.add("test");
        return arr;
    }

    @SwitchLogger(tag = "bar", extra = {"some", "thing"})
    public String bar(String arr) {
        return "arr";
    }

    public String other() {
        return "test";
    }
}
```

### Tips
- 关于 TrackId 的使用

框架默认支持自定义 `track-id`, `x-track-id`, `transaction-id`, `x-transaction-id` 四种 trackId 作为自定义 header
> `TrackId` 主要用于我们在进行多层链路调用时的唯一标识  
> 如果我们在 header 中指定了上述的其中一种，则框架默认会使用我们自定义的值，否则框架会自动生成一个 UUID 作为 trackId 的值