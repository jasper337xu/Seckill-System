package com.jasperxu.seckill.db.dao;

import com.jasperxu.seckill.db.models.Order;

public interface OrderDao {

    void insertOrder(Order order);

    Order queryOrder(String orderNo);

    void updateOrder(Order order);
}
