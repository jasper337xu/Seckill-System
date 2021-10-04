package com.jasperxu.seckill.service;

import com.alibaba.fastjson.JSON;
import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.po.Order;
import com.jasperxu.seckill.db.po.SeckillActivity;
import com.jasperxu.seckill.mq.RocketMQService;
import com.jasperxu.seckill.util.RedisService;
import com.jasperxu.seckill.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SeckillActivityService {

    @Resource
    private RedisService redisService;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RocketMQService rocketMQService;

    private SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    /**
     * Create an order
     * @param seckillActivityId
     * @param userId
     * @return
     * @throws Exception
     */
    public Order createOrder(long seckillActivityId, long userId) throws Exception {
        /*
         * 1. Create the order
         */
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        // Use Snowflake algorithm to generate ID for the order
        order.setOrderNo(String.valueOf(snowflakeIdGenerator.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        /*
         * 2.Use RocketMQ to send a message of the newly-created order
         */
        rocketMQService.sendMessage("order", JSON.toJSONString(order));

        return order;
    }

    public boolean validateStock(long seckillActivityId) {
        String key = "stock:" + seckillActivityId;
        return redisService.validateStockDeduction(key);
    }
}
