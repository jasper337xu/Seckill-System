package com.jasperxu.seckill;

import com.jasperxu.seckill.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisService redisService;

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
}
