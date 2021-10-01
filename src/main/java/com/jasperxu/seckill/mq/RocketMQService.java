package com.jasperxu.seckill.mq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RocketMQService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendMessage(String topic, String body) throws Exception {
        Message message = new Message(topic, body.getBytes());
        DefaultMQProducer producer = rocketMQTemplate.getProducer();
        producer.setSendMsgTimeout(60000);
        producer.send(message);
    }

    // public void sendDelayMessage(String topic, String body, int delayTimeLevel) throws Exception
}
