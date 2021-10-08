package com.jasperxu.seckill;

import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.mappers.SeckillActivityMapper;
import com.jasperxu.seckill.db.models.SeckillActivity;

import java.math.BigDecimal;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DaoTest {
    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Test
    void SeckillActivityTest() {
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName("Activity 1");
        seckillActivity.setCommodityId(999L);
        seckillActivity.setTotalStock(100L);
        seckillActivity.setSeckillPrice(new BigDecimal(99));
        seckillActivity.setActivityStatus(1);
        seckillActivity.setOldPrice(new BigDecimal(199));
        seckillActivity.setAvailableStock(100);
        seckillActivity.setLockStock(0L);
        seckillActivityMapper.insert(seckillActivity);
        System.out.println("====>>>>" + seckillActivityMapper.selectByPrimaryKey(1L));
    }

    @Test
    void setSeckillActivityQuery() {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitiesByStatus(0);
        System.out.println(seckillActivities.size());
        seckillActivities.stream().forEach(seckillActivity -> System.out.println(seckillActivity.toString()));
    }
}
