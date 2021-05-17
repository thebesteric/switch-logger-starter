package com.sourceflag.framework.switchlogger.redis.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.redis.configuration.mark.SwitchLoggerRedisMarker;
import com.sourceflag.framework.switchlogger.redis.controller.SwitchLoggerController;
import com.sourceflag.framework.switchlogger.redis.record.RedisRecordProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

/**
 * SwitchLoggerRedisAutoConfiguration
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2021-05-14 16:28
 * @since 1.0
 */
@Configuration
@Import(SwitchLoggerRedisInitialization.class)
@EnableConfigurationProperties(SwitchLoggerProperties.class)
public class SwitchLoggerRedisAutoConfiguration {

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

    @Bean
    @Conditional(SwitchLoggerRedisMarker.class)
    public RecordProcessor redisRecordProcessor(@Qualifier("switchLoggerRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                                SwitchLoggerProperties properties) {
        return new RedisRecordProcessor(redisTemplate, properties);
    }

    @Bean
    public SwitchLoggerController switchLoggerController(ApplicationContext context) {
        return new SwitchLoggerController(context);
    }
}
