package com.jasperxu.seckill.component;

import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.models.SeckillActivity;
import com.jasperxu.seckill.util.RedisService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisPreheatRunner implements ApplicationRunner {
    @Resource
    RedisService redisService;

    @Resource
    SeckillActivityDao seckillActivityDao;

    // When the application starts running, preload commodity stocks from db into Redis
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitiesByStatus(1);
        for (SeckillActivity seckillActivity : seckillActivities) {
            String key = "stock:" + seckillActivity.getId();
            redisService.setValue(key, (long) seckillActivity.getAvailableStock());
            // testing
            if (key.equals("stock:32")) {
                Long value = new Long(redisService.getValue("stock:32"));
                System.out.println("Initially, there are " + value + " stocks available for SeckillActivity 32");
            }
        }
    }
}
