package com.sourceflag.framework.switchlogger.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;

import java.util.Set;

/**
 * JedisUtils
 *
 * @author Eric Joe
 * @version Ver 1.0
 * @build 2020-08-04 16:11
 */
public class JedisUtils {

    private JedisPool jedisPool;

    public JedisUtils(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    public void hSetEx(String key, String field, String value, int seconds) {
        hSetEx(0, key, field, value, seconds);
    }

    public void hSetEx(int db, String key, String field, String value, int seconds) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.hset(key, field, value);
            jedis.expire(key, seconds);
        }
    }

    public void hSet(String key, String field, String value) {
        hSet(0, key, field, value);
    }

    public void hSet(int db, String key, String field, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.hset(key, field, value);
        }
    }

    public String hGet(String key, String field) {
        return hGet(0, key, field);
    }

    public String hGet(int db, String key, String field) {
        String result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            result = jedis.hget(key, field);
        }
        return result;
    }

    public Long hDel(String key, String... fields) {
        Long result;
        try (Jedis jedis = getJedis()) {
            result = jedis.hdel(key, fields);
        }
        return result;
    }

    public boolean setNxAndEx(String key, String value, int seconds) {
        return setNxAndEx(0, key, value, seconds);
    }

    public boolean setNxAndEx(int db, String key, String value, int seconds) {
        String result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            SetParams setParams = new SetParams();
            setParams.ex(seconds);
            setParams.nx();
            result = jedis.set(key, value, setParams);
        }
        return "OK".equals(result);
    }

    public boolean setEx(String key, String value, int seconds) {
        return setEx(0, key, value, seconds);
    }

    public boolean setEx(int db, String key, String value, int seconds) {
        String result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            result = jedis.setex(key, seconds, value);
        }
        return "OK".equals(result);
    }

    public boolean set(String key, String value) {
        return set(0, key, value);
    }

    public boolean set(int db, String key, String value) {
        String result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            result = jedis.set(key, value);
        }
        return "OK".equals(result);
    }

    public String get(String key) {
        return get(0, key);
    }

    public String get(int db, String key) {
        String result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            result = jedis.get(key);
        }
        return result;
    }

    public void del(String key) {
        del(0, key);
    }

    public void del(int db, String key) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.del(key);
        }
    }

    public void zadd(int db, String key, double score, String member) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.zadd(key, score, member);
        }
    }

    public void zadd(String key, double score, String member) {
        zadd(0, key, score, member);
    }

    public void zpopmax(int db, String key, int count) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.zpopmax(key, count);
        }
    }

    public void zpopmax(String key, int count) {
        zpopmax(0, key, count);
    }

    public void zpopmax(int db, String key) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.zpopmax(key);
        }
    }

    public void zpopmax(String key) {
        zpopmax(0, key);
    }

    public void zpopmin(int db, String key, int count) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.zpopmin(key, count);
        }
    }

    public void zpopmin(String key, int count) {
        zpopmin(0, key, count);
    }

    public void zpopmin(int db, String key) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.zpopmin(key);
        }
    }

    public void zpopmin(String key) {
        zpopmin(0, key);
    }


    public long zremrangeByScore(int db, String key, double min, double max) {
        long result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            result = jedis.zremrangeByScore(key, min, max);
        }
        return result;
    }

    public void zremrangeByScore(String key, double min, double max) {
        zremrangeByScore(0, key, min, max);
    }

    public Set<String> zrange(int db, String key, long start, long stop, boolean reverse) {
        Set<String> result;
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            if (reverse) {
                result = jedis.zrevrange(key, start, stop);
            } else {
                result = jedis.zrange(key, start, stop);
            }
        }
        return result;
    }

    public Set<String> zrange(String key, long start, long stop, boolean reverse) {
        return zrange(0, key, start, stop, reverse);
    }

    public void flushAll() {
        try (Jedis jedis = getJedis()) {
            jedis.flushAll();
        }
    }

    public void flushDB(int db) {
        try (Jedis jedis = getJedis()) {
            jedis.select(db);
            jedis.flushDB();
        }
    }

}
