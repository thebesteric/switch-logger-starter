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
添加上 `@EnableSwitchLogger` 后，即可对所有接口按照默认规则进行全局控制层日志记录

### 配置


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
- RequestLoggerProcessor 接口: 可自定义日志处理规则
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

### @SwitchLogger 使用
@SwitchLogger 可作用于类或方法上，用于记录非 @Controller 层的方法执行过程