package com.jasperxu.seckill.db.dao;

import com.jasperxu.seckill.db.models.SeckillActivity;

import java.util.List;

public interface SeckillActivityDao {

    public List<SeckillActivity> querySeckillActivitiesByStatus(int activityStatus);

    public void insertSeckillActivity(SeckillActivity seckillActivity);

    public SeckillActivity querySeckillActivityById(long activityId);

    public void updateSeckillActivity(SeckillActivity seckillActivity);

    public boolean lockStock(Long seckillActivityId);

    public boolean deductStock(Long seckillActivityId);

    public void revertStock(Long seckillActivityId);
}
