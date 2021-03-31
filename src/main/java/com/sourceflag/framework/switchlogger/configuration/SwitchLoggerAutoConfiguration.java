package com.sourceflag.framework.switchlogger.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sourceflag.framework.switchlogger.configuration.marker.SwitchLoggerCacheMarker;
import com.sourceflag.framework.switchlogger.configuration.marker.SwitchLoggerDatabaseMarker;
import com.sourceflag.framework.switchlogger.configuration.marker.SwitchLoggerMarker;
import com.sourceflag.framework.switchlogger.configuration.marker.SwitchLoggerRedisMarker;
import com.sourceflag.framework.switchlogger.controller.SwitchLoggerController;
import com.sourceflag.framework.switchlogger.core.SwitchLoggerFilter;
import com.sourceflag.framework.switchlogger.core.SwitchLoggerInitialization;
import com.sourceflag.framework.switchlogger.core.exception.ParseErrorException;
import com.sourceflag.framework.switchlogger.core.processor.*;
import com.sourceflag.framework.switchlogger.core.processor.attribute.AutowiredAttributeProcessor;
import com.sourceflag.framework.switchlogger.core.processor.attribute.ResourceAttributeProcessor;
import com.sourceflag.framework.switchlogger.core.processor.attribute.ValueAttributeProcessor;
import com.sourceflag.framework.switchlogger.core.processor.mapping.*;
import com.sourceflag.framework.switchlogger.core.processor.record.*;
import com.sourceflag.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import com.sourceflag.framework.switchlogger.core.scaner.annotated.SwitchLoggerAnnotatedEnhancer;
import com.sourceflag.framework.switchlogger.core.scaner.controller.SwitchLoggerControllerScanner;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableAsync;

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
public class SwitchLoggerAutoConfiguration {

    @Resource
    private Environment environment;

    @Bean(name = "switchLoggerFilterRegister")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public FilterRegistrationBean filterRegister(SwitchLoggerProperties properties, List<RecordProcessor> recordProcessors,
                                                 @Nullable RequestLoggerProcessor requestLoggerProcessor,
                                                 @Nullable IgnoreUrlProcessor ignoreUrlProcessor) {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setName(properties.getFilter().getName());
        frBean.setFilter(new SwitchLoggerFilter(properties, recordProcessors, requestLoggerProcessor, ignoreUrlProcessor));
        frBean.setOrder(properties.getFilter().getOrder());
        frBean.addUrlPatterns(properties.getFilter().getUrlPatterns());
        return frBean;
    }

    @Bean(name = "switchLoggerControllerScanner")
    public SwitchLoggerScanner switchLoggerControllerScanner(List<MappingProcessor> mappingProcessors) {
        return new SwitchLoggerControllerScanner(mappingProcessors);
    }

    @Bean(name = "switchLoggerAnnotatedEnhancer")
    public SwitchLoggerAnnotatedEnhancer switchLoggerAnnotatedEnhancer(ConfigurableListableBeanFactory factory,
                                                                       SwitchLoggerProperties properties,
                                                                       List<RecordProcessor> recordProcessors,
                                                                       List<AttributeProcessor> attributeProcessors) {
        return new SwitchLoggerAnnotatedEnhancer(factory, properties, recordProcessors, attributeProcessors);
    }

    @Bean(name = "switchLoggerRedisTemplate")
    @Conditional(SwitchLoggerRedisMarker.class)
    public RedisTemplate<String, Object> redisTemplate(@Nullable LettuceConnectionFactory lettuceConnectionFactory, SwitchLoggerProperties properties) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        if (lettuceConnectionFactory != null) {
            lettuceConnectionFactory.setDatabase(properties.getRedis().getDatabase());
            template.setConnectionFactory(lettuceConnectionFactory);
        }

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jacksonSerializer.setObjectMapper(objectMapper);

        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);
        template.setDefaultSerializer(jacksonSerializer);
        template.afterPropertiesSet();
        return template;
    }

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

    // AttributeProcessor

    @Bean(name = "switchLoggerAutowiredAttributeProcessor")
    public AttributeProcessor autowiredAttributeProcessor() {
        return new AutowiredAttributeProcessor();
    }

    @Bean(name = "switchLoggerResourceAttributeProcessor")
    public AttributeProcessor resourceAttributeProcessor() {
        return new ResourceAttributeProcessor();
    }

    @Bean(name = "switchLoggerValueAttributeProcessor")
    public AttributeProcessor valueAttributeProcessor() {
        return new ValueAttributeProcessor();
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

    // byDefault
    @Bean(name = "switchLoggerLogRecordProcessor")
    public RecordProcessor logRecordProcessor(SwitchLoggerProperties properties) {
        return new LogRecordProcessor(properties);
    }

    @Bean(name = "switchLoggerStdoutRecordProcessor")
    public RecordProcessor stdoutRecordProcessor(SwitchLoggerProperties properties) {
        return new StdoutRecordProcessor(properties);
    }

    @Bean
    @Conditional(SwitchLoggerRedisMarker.class)
    public RecordProcessor redisRecordProcessor(@Qualifier("switchLoggerRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                                SwitchLoggerProperties properties) {
        return new RedisRecordProcessor(redisTemplate, properties);
    }

    @Bean
    @Conditional(SwitchLoggerCacheMarker.class)
    public RecordProcessor cacheRecordProcessor(@Qualifier("switchLoggerCache") Cache<String, Object> switchLoggerCache,
                                                SwitchLoggerProperties properties) {
        return new CacheRecordProcessor(switchLoggerCache, properties);
    }

    @Bean
    @Conditional(SwitchLoggerDatabaseMarker.class)
    public RecordProcessor databaseRecordProcessor(@Qualifier("switchLoggerJdbcTemplate") SwitchJdbcTemplate switchLoggerJdbcTemplate,
                                                   SwitchLoggerProperties properties) {
        return new DatabaseRecordProcessor(switchLoggerJdbcTemplate, properties);
    }

    @Bean(name = "switchLoggerCache")
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
