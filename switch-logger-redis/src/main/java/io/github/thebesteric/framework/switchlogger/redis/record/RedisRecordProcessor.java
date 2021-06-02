package io.github.thebesteric.framework.switchlogger.redis.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.thebesteric.framework.switchlogger.configuration.SwitchLoggerProperties;
import io.github.thebesteric.framework.switchlogger.core.domain.InvokeLog;
import io.github.thebesteric.framework.switchlogger.core.processor.RecordProcessor;
import io.github.thebesteric.framework.switchlogger.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * RedisRecordProcessor
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-10-02 00:56
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRecordProcessor implements Runnable, RecordProcessor {

    private final RedisTemplate<String, Object> redisTemplate;

    private final SwitchLoggerProperties properties;

    @Override
    public boolean supports(String model) {
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(InvokeLog invokeLog) throws JsonProcessingException {
        // TODO 后期修改为 Redis 发布订阅模式
        redisTemplate.opsForZSet().add(properties.getRedis().getKey(), JsonUtils.mapper.writeValueAsString(invokeLog), invokeLog.getCreatedTime());
    }

    @Override
    public void run() {
        removeExpired();
    }

    private void removeExpired() {
        Long result = redisTemplate.opsForZSet().removeRangeByScore(properties.getRedis().getKey(), 0, System.currentTimeMillis() - properties.getRedis().getExpiredTime() * 1000L);
        log.debug("Clear {} expired data from Redis", result == null ? 0 : result);
    }
}
