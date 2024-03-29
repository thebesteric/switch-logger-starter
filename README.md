## Global log Framework

> 基于 Spring Boot 的一款全局控制层日志记录插件

### Quick start
[Download By Maven Center](https://search.maven.org/search?q=g:io.github.thebesteric.framework.switchlogger)
```xml
<dependency>
  <groupId>io.github.thebesteric.framework.switchlogger</groupId>
  <artifactId>switch-logger-core</artifactId>
  <version>${latest.version}</version>
</dependency>
```

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

---

当然我们也可以在 Service 层，Dao 层 分别在类上或方法上添加`@SwitchLogger`注解来监控方法执行过程
> 当 Controller 层调用 Service 层，再有 Service 层调用 Dao 层的话，会串联为一个**日志跟踪链**

```java
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private TestFacade testFacade;

    @GetMapping
    public R test(@RequestParam String name) {
        String wording = testFacade.sayHello(name, new Date());
        return R.success().setData(wording);
    }

}

@Component
@SwitchLogger(tag = "facade")
public class TestFacade {

    @Autowired
    private TestService testService;

    @SwitchLogger(extra = "say hello to somebody")
    public String sayHello(String name, Date date) {
        return testService.say(name) + " at " + date.toLocaleString();
    }

}

@Component
@SwitchLogger(tag = "service", level = "DEBUG")
public class TestService {

    public String say(String name) {
        return "hello " + name;
    }

}

// [17:17:38:702] [INFO] - LogRecordProcessor.processor(LogRecordProcessor.java:28) - {"tag":"service","level":"DEBUG","result":"hello 张三","exception":null,"extra":["just say hello to somebody"],"track_id":"F0BBA7C71D0D45D590AA6FA0C7822F89","created_time":1610875058681,"execute_info":{"className":"org.sourceflag.TestService","methodInfo":{"methodName":"say","signatures":{"name":"java.lang.String"},"arguments":{"name":"张三"},"returnType":"java.lang.String"},"duration":3,"start_time":1610875058675}}
// [17:17:38:703] [INFO] - LogRecordProcessor.processor(LogRecordProcessor.java:28) - {"tag":"facade","level":"INFO","result":"hello 张三 at 2021年1月17日 下午5:17:38","exception":null,"extra":["say hello to somebody with current date"],"track_id":"F0BBA7C71D0D45D590AA6FA0C7822F89","created_time":1610875058683,"execute_info":{"className":"org.sourceflag.TestFacade","methodInfo":{"methodName":"sayHello","signatures":{"name":"java.lang.String","date":"java.util.Date"},"arguments":{"name":"张三","date":1610875058666},"returnType":"java.lang.String"},"duration":15,"start_time":1610875058668}}
// [17:17:38:732] [INFO] - LogRecordProcessor.processor(LogRecordProcessor.java:28) - {"tag":"default","level":"INFO","result":{"code":200,"data":"hello 张三 at 2021年1月17日 下午5:17:38","message":"SUCCEED","timestamp":1610875058688},"exception":null,"extra":null,"uri":"/test","url":"http://127.0.0.1:8081/test","method":"GET","protocol":"HTTP/1.1","cookies":[],"headers":{"x-customer-id":"123456","content-length":"195","postman-token":"861a8531-a70d-472f-93fc-6ed195282e38","host":"127.0.0.1:8081","content-type":"application/json","connection":"keep-alive","cache-control":"no-cache","accept-encoding":"gzip, deflate, br","x-app-version":"1","user-agent":"PostmanRuntime/7.26.8","accept":"*/*"},"params":{"sign":"123","name":"张三","lang":"zh"},"body":{"cartCode":"1","address":{"province":"上海","city":"上海市","district":"浦东新区","receivedAddress":"南京西路23弄"}},"track_id":"F0BBA7C71D0D45D590AA6FA0C7822F89","created_time":1610875058717,"execute_info":{"className":"org.sourceflag.TestController","methodInfo":{"methodName":"test","signatures":{"name":"java.lang.String"},"arguments":{},"returnType":"org.sourceflag.controller.R"},"duration":85,"start_time":1610875058631},"request_session_id":null,"server_name":"127.0.0.1","remote_addr":"127.0.0.1","remote_port":5818,"query_string":"sign=123&lang=zh&name=%E5%BC%A0%E4%B8%89","origin_body":"{    \"cartCode\" : \"1\",    \"address\": {        \"province\":\"上海\",        \"city\":\"上海市\",        \"district\":\"浦东新区\",        \"receivedAddress\": \"南京西路23弄\"    }}"}

```

### 日志格式

日志格式包括 API 层日志格式和方法调用层日志格式两种  
- API 层日志格式包括一些常见的 HTTP 请求相关的信息，以及 API 执行信息
- 方法调用层（Service、Dao）的日志格式包括方法执行的相关信息

#### API 层日志格式
```json
{
    "tag": "default",
    "level": "INFO",
    "result": {
        "code": 200,
        "data": "hello 张三 at 2021年1月17日 下午5:03:25",
        "message": "SUCCEED",
        "timestamp": 1610874205896
    },
    "exception": null,
    "extra": null,
    "uri": "/test",
    "url": "http://127.0.0.1:8081/test",
    "method": "GET",
    "protocol": "HTTP/1.1",
    "cookies": [],
    "headers": {
        "x-customer-id": "123456",
        "content-length": "195",
        "postman-token": "d4b39f31-079a-4f52-b3e1-4d6c2545e8eb",
        "host": "127.0.0.1:8081",
        "content-type": "application/json",
        "connection": "keep-alive",
        "cache-control": "no-cache",
        "accept-encoding": "gzip, deflate, br",
        "x-app-version": "1",
        "user-agent": "PostmanRuntime/7.26.8",
        "accept": "*/*"
    },
    "params": {
        "sign": "123",
        "name": "张三",
        "lang": "zh"
    },
    "body": {
        "cartCode": "1",
        "address": {
            "province": "上海",
            "city": "上海市",
            "district": "浦东新区",
            "receivedAddress": "南京西路23弄"
        }
    },
    "track_id": "F0BBA7C71D0D45D590AA6FA0C7822F89",
    "created_time": 1610874205898,
    "execute_info": {
        "className": "org.sourceflag.TestController",
        "methodInfo": {
            "methodName": "test",
            "signatures": {
                "name": "java.lang.String"
            },
            "arguments": {},
            "returnType": "org.sourceflag.assets.controller.R"
        },
        "duration": 4,
        "start_time": 1610874205894
    },
    "request_session_id": null,
    "server_name": "127.0.0.1",
    "remote_addr": "127.0.0.1",
    "remote_port": 5518,
    "query_string": "sign=123&lang=zh&name=%E5%BC%A0%E4%B8%89",
    "duration": 66,
    "origin_body": "{    \"cartCode\" : \"1\",    \"address\": {        \"province\":\"上海\",        \"city\":\"上海市\",        \"district\":\"浦东新区\",        \"receivedAddress\": \"南京西路23弄\"    }}"
}
```
#### 方法调用层日志格式
```json
{
    "tag": "facade",
    "level": "INFO",
    "result": "hello 张三 at 2021年1月17日 下午5:17:38",
    "exception": null,
    "extra": [
        "say hello to somebody with current date"
    ],
    "track_id": "F0BBA7C71D0D45D590AA6FA0C7822F89",
    "created_time": 1610875058683,
    "execute_info": {
        "className": "org.sourceflag.TestFacade",
        "methodInfo": {
            "methodName": "sayHello",
            "signatures": {
                "name": "java.lang.String",
                "date": "java.util.Date"
            },
            "arguments": {
                "name": "张三",
                "date": 1610875058666
            },
            "returnType": "java.lang.String"
        },
        "duration": 15,
        "start_time": 1610875058668
    }
}
```

### 配置
```yaml
sourceflag.switch-logger:
    enable: true # available or not, default is true
    model: log # logging mode, support log, stdout, mysql, redis, local cache
```
- LOG 配置模式
> 输出到日志文件
```yaml
sourceflag.switch-logger:
    model: log
```
- STDOUT 配置模式
> 输出到控制台
```yaml
sourceflag.switch-logger:
    model: stdout
```
- MySQL 配置模式
> 写到数据库指定表中（支持自动建表，无需手工维护表）
```yaml
sourceflag.switch-logger:
    model: database
    database:
      tableName: SWITCH_LOGGER # table prefix
      url: "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&userSSL=false&serverTimezone=Asia/Shanghai"
      driverClassName: "com.mysql.cj.jdbc.Driver"
      username: "root"
      password: "root"
```
- Cache 配置模式
> 根据的配置来决定日志保留的数量与时间
```yaml
sourceflag.switch-logger:
    model: cache
    cache:
      initial-capacity: 2000
      maximum-size: 20000
      expired-time: 3600
```
- Redis 配置模式
> 根据配置来决定日志保留的时间
```yaml
sourceflag.switch-logger:
    model: redis
    redis:
      key: SWITCH_LOGGER
      database: 0
      expired-time: 3600
```
- Filter 自定义拦截位置
```yaml
sourceflag.switch-logger:
    model: log
    filter:
      url-patterns: ["/*"]
      include: [".*"]
      exclude: []
```
- GlobalResponse 全局成功返回处理机制
> use-default: 使用默认全局成功返回处理机器  
> response-entities: 定义全局正常的返回 code 字段与值，支持定义多组  
> message-fields: 定义返回信息的字段名称
```yaml
sourceflag:
  switch-logger:
    global-success-response:
      use-default: false # you can use default global success response if use-default is true
      response-entities:
        - code-field: code
          code-value: 200
        - code-field: code
          code-value: 100
      message-fields:
        - msg
        - message
```
- PrivateMethodLogging 私有方法是否记录日志
> 默认值为 false，系统默认不对私有方法进行日志记录，如有需要可以设置为 true  
> 当设置为 true 时，如不希望记录该方法的调用信息时，可以配合 @IgnoreSwitchLogger 进行忽略
```yaml
sourceflag:
  switch-logger:
    private-method-logging: true
```
- 使用 SkyWalking 的 traceId 代替 trackId
> 当设置为 true 时，使用 SkyWalking 的 traceId 来替代默认的 trackId
```yaml
sourceflag:
  switch-logger:
    sky-walking-trace: true
```
- RPC 监控配置
> 监控 RPC 客户端，目前支持 feign 和 forest
> succeed-codes: 指定 http 成功响应的状态码
```yaml
sourceflag:
  switch-logger:
    rpc:
      feign:
        enable: true
        succeed-codes: [100, 200]
      forest:
        enable: false
        succeed-codes: [100, 200]
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
        return method.isAnnotationPresent(RequestMapping.class);
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

- RequestLoggerProcessor 接口: 可自定义日志内容（暂不支持数据库模式）
```java
@EnableSwitchLogger
@Configuration
public class SwitchLoggerConfiguration {
    @Bean
    public RequestLoggerProcessor requestLoggerProcessor() {
        return new RequestLoggerProcessor() {
            @Override
            public RequestLog processor(SwitchLoggerRequestWrapper switchLoggerRequestWrapper, SwitchLoggerResponseWrapper switchLoggerResponseWrapper, Map<String, Method> map, long duration) throws IOException {
                RequestLog requestLog = new RequestLog();
                requestLog.setBody("Let me control");
                return requestLog;
            }
        };
    }
}
``` 

- AbstractRequestLoggerProcessor 抽象类: 继承 DefaultRequestLoggerProcessor 接口，可自定义日志处理规则
```java
public class SwitchLoggerConfiguration {
    @Bean
    public RequestLoggerProcessor requestLoggerProcessor() {
        return new AbstractRequestLoggerProcessor() {
            @Override
            public RequestLog doAfterProcessor(RequestLog requestLog) {
                requestLog.setRawBody(requestLog.getRawBody().substring(0, 20));
                return requestLog;
            }
        };
    }
}
```

- InterfaceMetricRequestLoggerProcessor 接口统计类: 继承 AbstractRequestLoggerProcessor 类，实现了接口统计功能
```java
@Configuration
public class SwitchLoggerConfiguration {
    @Bean
    public RequestLoggerProcessor requestLoggerProcessor() {
        return new InterfaceMetricRequestLoggerProcessor();
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

- GlobalResponseProcessor 接口：自定义全局成功返回处理
> 当项目自定义全局异常处理器后，可能 SwitchLogger 就无法捕捉到异常了，此时可以扩展此接口，指明异常判断方式  
> method: 表示当前执行的方法，当是 controller 层执行的时候，method 为空  
> result: 为程序返回结果
```java
@Configuration
public class SwitchLoggerConfiguration {
    @Bean
    public GlobalSuccessResponseProcessor globalSuccessResponseProcessor() {
        // method 当前执行的方法
        // result 为程序返回结果
        return (method, result) -> "you can check result and return error message";
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

> 项目中可以使用 `TransactionUtils.get()` 来获取`trackId` 


- 关于 @IgnoreParam

`@IgnoreParam` 注解，用来标记在 Controller 方法上的指定参数上，用来忽略相关参数

> 当使用了 `@IgnoreParam` 注解后，日志将不会记录真实值，只会按照指定的字符串记录
```java
public class RegisterController {
    @PostMapping("/register")
    public Object register(@RequestParam @IgnoreParam("******") String mobile) {
        return "success";
    }
}
```

- 关于 @IgnoreSwitchLogger

`@IgnoreSwitchLogger` 注解，用来标记在类或方法上，用来忽略记录日志

> 当在类上使用了 `@IgnoreSwitchLogger` 注解后，日志将不会记录该类下所有方法到日志情况  
> `@IgnoreSwitchLogger` 与 `@SwitchLogger` 同时存在时，`@IgnoreSwitchLogger` 优先级更高
```java
public class BaseService {
    @IgnoreSwitchLogger
    public List<String> foo(List<String> arr) {
        arr.add("test");
        return arr;
    }
}
```