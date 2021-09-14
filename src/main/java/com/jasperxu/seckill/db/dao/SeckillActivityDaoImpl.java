package com.jasperxu.seckill.db.dao;

import com.jasperxu.seckill.db.mappers.SeckillActivityMapper;
import com.jasperxu.seckill.db.po.SeckillActivity;

import org.springframework.stereotype.Repository;
import javax.annotation.Resource;
import java.util.List;

@Repository
public class SeckillActivityDaoImpl implements SeckillActivityDao {

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Override
    public List<SeckillActivity> querySeckillActivitiesByStatus(int activityStatus) {
        return seckillActivityMapper.querySeckillActivitiesByStatus(activityStatus);
    }

    @Override
    public void insertSeckillActivity(SeckillActivity seckillActivity) {
        seckillActivityMapper.insert(seckillActivity);
    }

    @Override
    public SeckillActivity querySeckillActivityById(long activityId) {
        return seckillActivityMapper.selectByPrimaryKey(activityId);
    }

    @Override
    public void updateSeckillActivity(SeckillActivity seckillActivity) {
        seckillActivityMapper.updateByPrimaryKey(seckillActivity);
    }
}
