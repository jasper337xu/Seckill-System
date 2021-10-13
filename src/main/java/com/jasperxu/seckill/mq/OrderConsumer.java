package com.jasperxu.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.jasperxu.seckill.db.dao.OrderDao;
import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.models.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "order", consumerGroup = "order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Override
    public void onMessage (MessageExt messageExt) {
        // 1. Parse messages into orders
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received request for creating an order：" + message);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(new Date());

        // 2. Lock stock
        boolean lockStockResult = seckillActivityDao.lockStock(order.getSeckillActivityId());
        if (lockStockResult) {
            // Order Status 1: Order created, waiting for checkout
            order.setOrderStatus(1);
        } else {
            // Order Status 0: No available stock，invalid order
            order.setOrderStatus(0);
        }

        // 3. Insert a new order(record) to database
        orderDao.insertOrder(order);
    }

}
