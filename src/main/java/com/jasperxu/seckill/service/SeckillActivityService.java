package com.jasperxu.seckill.service;

import com.jasperxu.seckill.util.RedisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SeckillActivityService {
    @Resource
    private RedisService redisService;

    public boolean validateAndProcessSeckillOrder(long seckillActivityId) {
        String key = "stock:" + seckillActivityId;
        return redisService.validateStockDeduction(key);
    }
}
