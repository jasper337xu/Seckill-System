package com.jasperxu.seckill.db.mappers;

import com.jasperxu.seckill.db.models.SeckillActivity;

import java.util.List;

public interface SeckillActivityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SeckillActivity record);

    int insertSelective(SeckillActivity record);

    SeckillActivity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SeckillActivity record);

    int updateByPrimaryKey(SeckillActivity record);

    List<SeckillActivity> querySeckillActivitiesByStatus(int activityStatus);

    int lockStock(Long seckillActivityId);

    int deductStock(Long seckillActivityId);

    void revertStock(Long seckillActivityId);
}