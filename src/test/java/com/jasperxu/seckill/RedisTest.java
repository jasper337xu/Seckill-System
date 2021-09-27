package com.jasperxu.seckill;

import com.jasperxu.seckill.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisService redisService;

    @Test
    public void stockTest(){
        redisService.setValue("stock:123", 10L);
    }

    @Test
    public void getStockTest() {
        String stock = redisService.getValue("stock:123");
        System.out.println(stock);
    }

    @Test
    public void validateStockDeductionTest(){
        boolean result = redisService.validateStockDeduction("stock:123");
        System.out.println("result: " + result);
        String stock = redisService.getValue("stock:123");
        System.out.println("Available stock after deduction: " + stock);
    }
}
