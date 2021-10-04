package com.jasperxu.seckill.controller;

import com.jasperxu.seckill.service.SeckillActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class SeckillOversellController {
//    @Resource
//    private SeckillOversellService seckillOversellService;

    @Resource
    private SeckillActivityService seckillActivityService;

//    /*
//    Uses the simple version method to process order,
//    NOT able to handle a large number of concurrent requests
//    */
//    @ResponseBody
//    @RequestMapping("/seckill/commodity/{seckillActivityId}")
//    public String processSeckillOrder(@PathVariable long seckillActivityId){
//        return seckillOversellService.processSeckillOrder(seckillActivityId);
//    }

    @ResponseBody
    @RequestMapping("/seckill/commodity/{seckillActivityId}")
    public String processSeckillOrder(@PathVariable long seckillActivityId){
        boolean stockValidationResult = seckillActivityService.validateStock(seckillActivityId);
        if (stockValidationResult) {
            return "Thanks for shopping! Your order has been processed!";
        }
        return "Sorry! The item you ordered has been sold out.";
    }
}
