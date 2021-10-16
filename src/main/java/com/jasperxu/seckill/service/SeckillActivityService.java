package com.jasperxu.seckill.service;

import com.alibaba.fastjson.JSON;
import com.jasperxu.seckill.db.dao.OrderDao;
import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.dao.SeckillCommodityDao;
import com.jasperxu.seckill.db.models.Order;
import com.jasperxu.seckill.db.models.SeckillActivity;
import com.jasperxu.seckill.db.models.SeckillCommodity;
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
    private SeckillCommodityDao seckillCommodityDao;

    @Resource
    OrderDao orderDao;

    private SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    /**
     * Create an order.
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

        /*
         * 3.Use RocketMQ to send a delay message to check payment status of the order.
         * Consumer won't consume message until the selected delay time period has passed.
         * If payment status of the order is unpaid at this point, the order will be closed.
         */
        rocketMQService.sendDelayMessage("payment_check", JSON.toJSONString(order), 5);

        return order;
    }

    public boolean validateStock(long seckillActivityId) {
        String key = "stock:" + seckillActivityId;
        return redisService.validateStockDeduction(key);
    }

    /**
     * Process order checkout.
     * @param orderNo
     */
    public void checkoutProcess(String orderNo) throws Exception {
        Order order = orderDao.queryOrder(orderNo);

        // 1. Check whether the order exists.
        if (order == null) {
            log.error("This order does not exist. Order No. " + orderNo);
            return;
        }

        // NOTE: In reality, we need to connect to third-party payment API to really process the payment.

        // 2. Order payment has been successfully processed.
        order.setPayTime(new Date());
        // Order Status 0: No available stock，invalid order
        //              1: Order created, waiting for checkout
        //              2: Checkout completed
        order.setOrderStatus(2);
        //orderDao.updateOrder(order);

        // 3. Send a message that order payment is completed.
        rocketMQService.sendMessage("payment_done", JSON.toJSONString(order));
    }

    /**
     * Warm up cache: load into Redis Cache the info of seckill activities and commodities.
     * @param seckillActivityId
     */
    public void loadSeckillInfoToRedisCache(long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:" + seckillActivityId, JSON.toJSONString(seckillActivity));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(), JSON.toJSONString(seckillCommodity));
    }
}
