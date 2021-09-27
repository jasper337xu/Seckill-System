package com.jasperxu.seckill.util;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;

@Service
public class RedisService {
    @Resource
    private JedisPool jedisPool;

    public void setValue(String key, Long value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value.toString());
        jedisClient.close();
    }

    public String getValue(String key) {
        Jedis jedisClient = jedisPool.getResource();
        String value = jedisClient.get(key);
        jedisClient.close();
        return value;
    }
}
