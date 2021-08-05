package io.github.thebesteric.framework.switchlogger.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.thebesteric.framework.switchlogger.configuration.marker.SwitchLoggerCacheMarker;
import io.github.thebesteric.framework.switchlogger.configuration.marker.SwitchLoggerMarker;
import io.github.thebesteric.framework.switchlogger.core.SwitchLoggerCoreInitialization;
import io.github.thebesteric.framework.switchlogger.core.SwitchLoggerFilter;
import io.github.thebesteric.framework.switchlogger.core.processor.*;
import io.github.thebesteric.framework.switchlogger.core.processor.mapping.*;
import io.github.thebesteric.framework.switchlogger.core.processor.record.CacheRecordProcessor;
import io.github.thebesteric.framework.switchlogger.core.processor.record.LogRecordProcessor;
import io.github.thebesteric.framework.switchlogger.core.processor.record.StdoutRecordProcessor;
import io.github.thebesteric.framework.switchlogger.core.processor.response.DefaultGlobalResponseProcessor;
import io.github.thebesteric.framework.switchlogger.core.scaner.SwitchLoggerScanner;
import io.github.thebesteric.framework.switchlogger.core.scaner.annotated.SwitchLoggerAnnotatedEnhancer;
import io.github.thebesteric.framework.switchlogger.core.scaner.controller.SwitchLoggerControllerScanner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SwitchLoggerConfig
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-09-29 23:45
 * @since 1.0
 */
@Configuration
@Import(SwitchLoggerCoreInitialization.class)
@EnableAsync
@ConditionalOnBean(SwitchLoggerMarker.class)
@EnableConfigurationProperties(SwitchLoggerProperties.class)
public class SwitchLoggerAutoConfiguration {

    @Bean(name = "switchLoggerFilterRegister")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public FilterRegistrationBean filterRegister(SwitchLoggerProperties properties, List<RecordProcessor> recordProcessors,
                                                 @Nullable RequestLoggerProcessor requestLoggerProcessor,
                                                 @Nullable IgnoreUrlProcessor ignoreUrlProcessor,
                                                 GlobalResponseProcessor globalResponseProcessor) {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setName(properties.getFilter().getName());
        frBean.setFilter(new SwitchLoggerFilter(properties, recordProcessors, requestLoggerProcessor,
                ignoreUrlProcessor, globalResponseProcessor));
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
                                                                       List<AttributeProcessor> attributeProcessors,
                                                                       GlobalResponseProcessor globalResponseProcessor) {
        return new SwitchLoggerAnnotatedEnhancer(factory, properties, recordProcessors, attributeProcessors, globalResponseProcessor);
    }

    // Global Response

    @Bean(name = "switchGlobalResponseProcessor")
    @ConditionalOnMissingBean(GlobalResponseProcessor.class)
    public GlobalResponseProcessor globalResponseProcessor(SwitchLoggerProperties properties) {
        return new DefaultGlobalResponseProcessor(properties);
    }

    // Mapping Processor

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

    // Record Processor

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
    @Conditional(SwitchLoggerCacheMarker.class)
    public RecordProcessor cacheRecordProcessor(@Qualifier("switchLoggerCache") Cache<String, Object> switchLoggerCache,
                                                SwitchLoggerProperties properties) {
        return new CacheRecordProcessor(switchLoggerCache, properties);
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
}
