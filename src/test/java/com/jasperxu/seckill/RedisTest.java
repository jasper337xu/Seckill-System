package com.jasperxu.seckill;

import com.jasperxu.seckill.service.SeckillActivityService;
import com.jasperxu.seckill.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Test
    public void setStockTest(){
        redisService.setValue("stock:123", 10L);
        String value = redisService.getValue("stock:123");
        assertEquals(new Long(value), 10L);
    }

    @Test
    public void getStockTest() {
        String value = redisService.getValue("stock:123");
        assertEquals(new Long(value), 10L);
    }

    @Test
    public void validateStockDeductionTest(){
        boolean result = redisService.validateStockDeduction("stock:123");
        assertTrue(result);
        String value = redisService.getValue("stock:123");
        assertEquals(new Long(value), 9L);
    }

    @Test
    public void loadSeckillInfoToRedisCacheTest() {
        seckillActivityService.loadSeckillInfoToRedisCache(19);
    }

    @Test
    public void retrieveSeckillInfoFromRedisCacheTest() {
        String activityInfo = redisService.getValue("seckillActivity:19");
        System.out.println(activityInfo);
        String commodityInfo = redisService.getValue("seckillCommodity:1001");
        System.out.println(commodityInfo);
    }
}
