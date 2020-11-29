package com.sourceflag.framework.switchlogger.starter;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sourceflag.framework.switchlogger.controller.SwitchLoggerController;
import com.sourceflag.framework.switchlogger.core.SwitchLoggerFilter;
import com.sourceflag.framework.switchlogger.core.SwitchLoggerInitialization;
import com.sourceflag.framework.switchlogger.core.exception.ParseErrorException;
import com.sourceflag.framework.switchlogger.core.marker.SwitchLoggerCacheMarker;
import com.sourceflag.framework.switchlogger.core.marker.SwitchLoggerDatabaseMarker;
import com.sourceflag.framework.switchlogger.core.marker.SwitchLoggerRedisMarker;
import com.sourceflag.framework.switchlogger.core.processor.MappingProcessor;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.core.processor.mapping.*;
import com.sourceflag.framework.switchlogger.core.processor.record.*;
import com.sourceflag.framework.switchlogger.utils.JedisUtils;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SwitchLoggerConfig
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-09-29 23:45
 * @since 1.0
 */
@Configuration
@Import(SwitchLoggerInitialization.class)
@EnableAsync
@ConditionalOnBean(SwitchLoggerMarker.class)
@EnableConfigurationProperties(SwitchLoggerProperties.class)
public class SwitchLoggerConfig {

    @Resource
    private Environment environment;

    @Bean(name = "switchLoggerFilterRegister")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public FilterRegistrationBean filterRegister(SwitchLoggerProperties properties, List<RecordProcessor> recordProcessors) {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setName(properties.getFilter().getName());
        frBean.setFilter(new SwitchLoggerFilter(properties, recordProcessors));
        frBean.setOrder(properties.getFilter().getOrder());
        frBean.addUrlPatterns(properties.getFilter().getUrlPatterns());
        return frBean;
    }

    @Bean
    @Conditional(SwitchLoggerRedisMarker.class)
    @Qualifier("switchLoggerJedisPool")
    public JedisPool switchLoggerJedisPool() {
        String password = environment.getProperty("spring.redis.password");
        String host = environment.getProperty("spring.redis.host", "localhost");
        int port = Integer.parseInt(environment.getProperty("spring.redis.port", "6379"));
        int timeout = Integer.parseInt(environment.getProperty("spring.redis.timeout", "5000"));
        int maxIdle = Integer.parseInt(environment.getProperty("spring.redis.jedis.pool.max-idle", "20"));
        int minIdle = Integer.parseInt(environment.getProperty("spring.redis.jedis.pool.min-idle", "10"));
        int maxTotal = Integer.parseInt(environment.getProperty("spring.redis.jedis.pool.max-active", "100"));
        long maxWait = Long.parseLong(environment.getProperty("spring.redis.jedis.pool.max-wait", "5000"));
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        jedisPoolConfig.setTestOnBorrow(false);
        jedisPoolConfig.setTestOnReturn(false);
        jedisPoolConfig.setTestOnCreate(false);
        return password == null || password.trim().equals("") ?
                new JedisPool(jedisPoolConfig, host, port, timeout) :
                new JedisPool(jedisPoolConfig, host, port, timeout, password);
    }

    @Bean
    @Conditional(SwitchLoggerRedisMarker.class)
    @Qualifier("switchLoggerJedisUtils")
    public JedisUtils switchLoggerJedisUtils(@Qualifier("switchLoggerJedisPool") JedisPool switchLoggerJedisPool) {
        return new JedisUtils(switchLoggerJedisPool);
    }

    @Bean
    @Conditional(SwitchLoggerDatabaseMarker.class)
    @Qualifier("switchLoggerDatasource")
    public DruidDataSource dataSource(SwitchLoggerProperties properties) throws ParseErrorException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername((String) checkAndGetEnvironmentVariable(properties.getDatabase().getUsername()));
        dataSource.setPassword((String) checkAndGetEnvironmentVariable(properties.getDatabase().getPassword()));
        dataSource.setDriverClassName((String) checkAndGetEnvironmentVariable(properties.getDatabase().getDriverClassName()));
        dataSource.setUrl((String) checkAndGetEnvironmentVariable(properties.getDatabase().getUrl()));
        dataSource.setMaxActive(Runtime.getRuntime().availableProcessors() * 2 + 1); // CUP * 2 + 1
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(2);
        dataSource.setMaxWait(10000);
        return dataSource;
    }

    @Bean
    @Conditional(SwitchLoggerDatabaseMarker.class)
    @Qualifier("switchLoggerJdbcTemplate")
    public SwitchJdbcTemplate switchLoggerJdbcTemplate(@Qualifier("switchLoggerDatasource") DruidDataSource dataSource) {
        return new SwitchJdbcTemplate(dataSource);
    }

    // MappingProcessor

    @Bean(name = "switchLoggerRequestMappingProcessor")
    public MappingProcessor requestMappingProcessor() {
        return new RequestMappingProcessor();
    }

    @Bean(name = "switchLoggerDeleteMappingProcessor")
    public MappingProcessor deleteMappingProcessor() {
        return new DeleteMappingProcessor();
    }

    @Bean(name = "switchLoggerGetMappingProcessor")
    public MappingProcessor getMappingProcessor() {
        return new GetMappingProcessor();
    }

    @Bean(name = "switchLoggerPatchMappingProcessor")
    public MappingProcessor patchMappingProcessor() {
        return new PatchMappingProcessor();
    }

    @Bean(name = "switchLoggerPostMappingProcessor")
    public MappingProcessor postMappingProcessor() {
        return new PostMappingProcessor();
    }

    @Bean(name = "switchLoggerPutMappingProcessor")
    public PutMappingProcessor putMappingProcessor() {
        return new PutMappingProcessor();
    }

    // RecordProcessor

    @Bean(name = "switchLoggerLogRecordProcessor")
    public RecordProcessor logRecordProcessor() {
        return new LogRecordProcessor();
    }

    @Bean(name = "switchLoggerStdoutRecordProcessor")
    public RecordProcessor stdoutRecordProcessor() {
        return new StdoutRecordProcessor();
    }

    @Bean
    @Conditional(SwitchLoggerRedisMarker.class)
    public RecordProcessor redisRecordProcessor(@Qualifier("switchLoggerJedisUtils") JedisUtils jedisUtils, SwitchLoggerProperties properties) {
        return new RedisRecordProcessor(jedisUtils, properties);
    }

    @Bean
    @Conditional(SwitchLoggerCacheMarker.class)
    public RecordProcessor cacheRecordProcessor(@Qualifier("switchLoggerCache") Cache<String, Object> switchLoggerCache) {
        return new CacheRecordProcessor(switchLoggerCache);
    }

    @Bean
    @Conditional(SwitchLoggerDatabaseMarker.class)
    public RecordProcessor databaseRecordProcessor(@Qualifier("switchLoggerJdbcTemplate") SwitchJdbcTemplate switchLoggerJdbcTemplate, SwitchLoggerProperties properties) {
        return new DatabaseRecordProcessor(switchLoggerJdbcTemplate, properties);
    }

    @Bean
    @Qualifier("switchLoggerCache")
    @Conditional(SwitchLoggerCacheMarker.class)
    public Cache<String, Object> switchLoggerCache(SwitchLoggerProperties properties) {
        return Caffeine.newBuilder()
                .initialCapacity(properties.getCache().getInitialCapacity())
                .maximumSize(properties.getCache().getMaximumSize())
                .expireAfterWrite(properties.getCache().getExpiredTime(), TimeUnit.SECONDS)
                .build();
    }

    // controller

    @Bean
    public SwitchLoggerController switchLoggerController(ApplicationContext context) {
        return new SwitchLoggerController(context);
    }

    private static String regex = "\\$\\{(.*?)\\}";
    private static Pattern pattern = Pattern.compile(regex);

    private Object checkAndGetEnvironmentVariable(String var) throws ParseErrorException {
        Matcher matcher = pattern.matcher(var);
        while (matcher.find()) {
            String original = matcher.group(0);
            String property = matcher.group(1);
            String[] arr = property.split(":");
            if (arr.length > 2)
                throw new ParseErrorException(var + " is not the correct expression");

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
