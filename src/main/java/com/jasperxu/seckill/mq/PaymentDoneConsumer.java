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

@Slf4j
@Component
@RocketMQMessageListener(topic = "payment_done", consumerGroup = "payment_done_group")
public class PaymentDoneConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received a message of payment completed：" + message);
        Order order = JSON.parseObject(message, Order.class);

        // 1. Deduct stock
        seckillActivityDao.deductStock(order.getSeckillActivityId());

        // 2. Update the order (payment time and order status) to db
        orderDao.updateOrder(order);
    }
}
