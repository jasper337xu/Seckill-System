package com.jasperxu.seckill;

import com.jasperxu.seckill.mq.RocketMQService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RocketMQTest {

    @Autowired
    RocketMQService rocketMQService;

    @Test
    public void sendMessageTest() throws Exception {
        rocketMQService.sendMessage("test-topic", "Hello World!");
    }
}
