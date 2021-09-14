package com.jasperxu.seckill.db.dao;

import com.jasperxu.seckill.db.po.SeckillActivity;

import java.util.List;

public interface SeckillActivityDao {

    public List<SeckillActivity> querySeckillActivitiesByStatus(int activityStatus);

    public void insertSeckillActivity(SeckillActivity seckillActivity);

    public SeckillActivity querySeckillActivityById(long activityId);

    public void updateSeckillActivity(SeckillActivity seckillActivity);
}
