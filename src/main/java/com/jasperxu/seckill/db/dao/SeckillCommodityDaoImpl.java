package com.jasperxu.seckill.db.dao;

import com.jasperxu.seckill.db.mappers.SeckillCommodityMapper;
import com.jasperxu.seckill.db.models.SeckillCommodity;

import org.springframework.stereotype.Repository;
import javax.annotation.Resource;

@Repository
public class SeckillCommodityDaoImpl implements SeckillCommodityDao {

    @Resource
    private SeckillCommodityMapper seckillCommodityMapper;

    @Override
    public SeckillCommodity querySeckillCommodityById(long commodityId) {
        return seckillCommodityMapper.selectByPrimaryKey(commodityId);
    }
}
