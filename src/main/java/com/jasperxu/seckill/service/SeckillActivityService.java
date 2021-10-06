package com.jasperxu.seckill.service;

import com.alibaba.fastjson.JSON;
import com.jasperxu.seckill.db.dao.OrderDao;
import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.po.Order;
import com.jasperxu.seckill.db.po.SeckillActivity;
import com.jasperxu.seckill.mq.RocketMQService;
import com.jasperxu.seckill.util.RedisService;
import com.jasperxu.seckill.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class SeckillActivityService {

    @Resource
    private RedisService redisService;

    @Resource
    private RocketMQService rocketMQService;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    OrderDao orderDao;

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

    /**
     * Process checking out the order
     * @param orderNo
     */
    public void checkoutProcess(String orderNo) {
        Order order = orderDao.queryOrder(orderNo);
        boolean deductStockResult = seckillActivityDao.deductStock(order.getSeckillActivityId());
        if (deductStockResult) {
            log.info("Checkout completed! Order No. " + orderNo);
            order.setPayTime(new Date());
            // Order Status 0: No available stock，invalid order
            //              1: Order created, waiting for checkout
            //              2: Checkout completed
            order.setOrderStatus(2);
            orderDao.updateOrder(order);
        }
    }
}
