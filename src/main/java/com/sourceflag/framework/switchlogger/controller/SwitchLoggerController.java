package com.sourceflag.framework.switchlogger.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.utils.SwitchJdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

/**
 * SwitchLoggerRedisController
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-03 00:19
 * @since 1.0
 */
@RestController
@RequestMapping("/switch-logger")
@RequiredArgsConstructor
public class SwitchLoggerController {

    public final ApplicationContext context;

    @GetMapping("/query")
    @SuppressWarnings("unchecked")
    public Object query(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size) throws SQLException {
        SwitchLoggerProperties properties = properties();
        String model = properties.getModel();
        if (SwitchLoggerProperties.ModelType.CACHE.name().equalsIgnoreCase(model)) {
            return cache().asMap();
        } else if (SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(model)) {
            long start = (page - 1) * size;
            long end = start + size - 1;
            return redisTemplate().opsForZSet().range(properties.getRedis().getKey(), start, end);
        } else if (SwitchLoggerProperties.ModelType.DATABASE.name().equalsIgnoreCase(model)) {
            return jdbcTemplate().page(properties().getDatabase().getTableName(), page, size);
        }
        return null;
    }

    public SwitchLoggerProperties properties() {
        return context.getBean(SwitchLoggerProperties.class);
    }

    @SuppressWarnings("rawtypes")
    public Cache cache() {
        return context.getBean(Cache.class);
    }

    @SuppressWarnings("rawtypes")
    public RedisTemplate redisTemplate() {
        return context.getBean(RedisTemplate.class);
    }

    public SwitchJdbcTemplate jdbcTemplate() {
        return context.getBean(SwitchJdbcTemplate.class);
    }

}