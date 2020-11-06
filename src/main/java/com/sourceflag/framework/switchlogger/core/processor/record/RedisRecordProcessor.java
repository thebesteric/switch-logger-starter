package com.sourceflag.framework.switchlogger.core.processor.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourceflag.framework.switchlogger.core.RequestLog;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.utils.JedisUtils;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.TimerTask;

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
public class RedisRecordProcessor extends TimerTask implements RecordProcessor {

    @Qualifier("switchLoggerJedisUtils")
    private final JedisUtils jedisUtils;

    private final SwitchLoggerProperties properties;

    @Override
    public boolean supports(String model) {
        return model != null && !model.trim().equals("") && SwitchLoggerProperties.ModelType.REDIS.name().equalsIgnoreCase(model);
    }

    @Override
    public void processor(RequestLog requestLog) throws JsonProcessingException {
        // TODO 后期修改为 Redis 发布订阅模式
        jedisUtils.zadd(properties.getRedis().getDatabase(), properties.getRedis().getKey(), requestLog.getTimestamp(), JsonUtils.mapper.writeValueAsString(requestLog));
        // jedisUtils.setEx(properties.getRedis().getDatabase(), requestLog.getTrackId(),mapper.writeValueAsString(requestLog), properties.getRedis().getExpiredTime());
    }

    @Override
    public void run() {
        long result = jedisUtils.zremrangeByScore(properties.getRedis().getDatabase(), properties.getRedis().getKey(), 0, System.currentTimeMillis() - properties.getRedis().getExpiredTime() * 1000);
        log.debug("Clear {} expired data from Redis", result);
    }
}
