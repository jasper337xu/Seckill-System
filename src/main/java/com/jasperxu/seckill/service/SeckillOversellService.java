package com.jasperxu.seckill.service;

import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.po.SeckillActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOversellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    //TODO: Simple Version
    public String processSeckillOrder(long activityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(activityId);
        long availableStock = seckillActivity.getAvailableStock();
        String result;
        if (availableStock > 0) {
            result = "Congratulations! Ordered successfully!";
            System.out.println(result);
            availableStock -= 1;
            seckillActivity.setAvailableStock(new Integer("" + availableStock));
            seckillActivityDao.updateSeckillActivity(seckillActivity);
        } else {
            result = "Sorry! The item you ordered is already sold out.";
            System.out.println(result);
        }
        return result;
    }
}
