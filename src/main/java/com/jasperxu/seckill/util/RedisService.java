package com.jasperxu.seckill.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Service
public class RedisService {
    @Resource
    private JedisPool jedisPool;

    public void setValue(String key, Object value) {
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

    // validateStockDeduction checks if there is available stock by using Redis.
    // If there is available stock, Redis deducts available stock by 1 and the method returns true.
    // Otherwise, the method returns false.
    public boolean validateStockDeduction(String key) {
        try (Jedis jedisClient = jedisPool.getResource()) {
            String script =
                    "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "    local stock = tonumber(redis.call('get', KEYS[1]))\n" +
                    "    if (stock <= 0) then\n" +
                    "       return -1\n" +
                    "    end;\n" +
                    "    redis.call('decr',KEYS[1]);\n" +
                    "    return stock - 1;\n" +
                    "end;\n" +
                    "return -1;";
            Long stock = (Long) jedisClient.eval(script, Collections.singletonList(key), Collections.emptyList());

            if (stock < 0) {
                log.info("No Available Stock");
                return false;
            }
            log.info("Stock successfully deducted");
            return true;
        } catch (Throwable throwable) {
            log.error("Stock deduction failed：" + throwable.toString());
            return false;
        }
    }

    /**
     * Revert the stock stored in Redis when payment is not completed within limited time.
     * (Data consistency between DB and Redis)
     * @param key
     */
    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }
}
