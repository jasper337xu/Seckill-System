package com.jasperxu.seckill.controller;

import com.jasperxu.seckill.service.SeckillOversellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SeckillOversellController {
    @Autowired
    private SeckillOversellService seckillOversellService;

    @ResponseBody
    @RequestMapping("/seckill/order/{seckillActivityId}")
    public String processSeckillOrder(@PathVariable long seckillActivityId){
        return seckillOversellService.processSeckillOrder(seckillActivityId);
    }
}
