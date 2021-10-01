package com.jasperxu.seckill.mq;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
@RocketMQMessageListener(topic = "test-topic", consumerGroup = "consumer-group")
public class TestConsumer implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            String body = new String(messageExt.getBody(), "UTF-8");
            System.out.println("Received a message: " + body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
