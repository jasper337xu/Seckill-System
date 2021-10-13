package com.jasperxu.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.jasperxu.seckill.db.dao.OrderDao;
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
@RocketMQMessageListener(topic = "payment_check", consumerGroup = "payment_check_group")
public class PaymentStatusCheckConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private OrderDao orderDao;

    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received a message of payment status check：" + message);
        Order order = JSON.parseObject(message, Order.class);

        // 1. Query the order to get the payment status
        // Note: variable order parsed from message does not store payment status
        //       because we do not set orderStatus when message is sent to RocketMQ.
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());

        // 2. Check whether the order exists
        if (orderInfo == null) {
            log.info("Order is null. Order No. " + order.getOrderNo());
            return;
        }

        // 3. Check whether the payment of the order has been completed
        if (orderInfo.getOrderStatus() != 2) {
            // 4. Payment is not completed, close the order
            log.info("Payment not completed. Order closed. Order No. " + orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);

            // 5. Revert the stock

            // 6. Remove the user from the list of users that have already purchased
        }
    }
}
