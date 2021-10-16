package com.jasperxu.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.jasperxu.seckill.db.dao.OrderDao;
import com.jasperxu.seckill.db.dao.SeckillActivityDao;
import com.jasperxu.seckill.db.dao.SeckillCommodityDao;
import com.jasperxu.seckill.db.models.Order;
import com.jasperxu.seckill.db.models.SeckillActivity;
import com.jasperxu.seckill.db.models.SeckillCommodity;
import com.jasperxu.seckill.service.SeckillActivityService;
import com.jasperxu.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private SeckillCommodityDao seckillCommodityDao;

    @Resource
    OrderDao orderDao;

    @Resource
    SeckillActivityService seckillActivityService;

    @Resource
    RedisService redisService;

    @RequestMapping("/seckillActivities")
    public String getAllOngoingSeckillActivities(Map<String, Object> resultMap) {
        // query all ongoing seckill activities
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitiesByStatus(1);
        resultMap.put("seckillActivities", seckillActivities);
        return "seckill_activity";
    }

    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String,Object> resultMap,@PathVariable long seckillActivityId) {
        SeckillActivity seckillActivity;
        SeckillCommodity seckillCommodity;

        String activityInfo = redisService.getValue("seckillActivity:" + seckillActivityId);
        if (activityInfo == null || activityInfo.equals("")) {
            seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        }
        else {
            // Seckill activity has been stored into Redis Cache.
            // Retrieving from cache is much faster than retrieving from database.
            log.info("Seckill activity info retrieved from Redis Cache: " + activityInfo);
            seckillActivity = JSON.parseObject(activityInfo, SeckillActivity.class);
        }

        String commodityInfo = redisService.getValue("seckillCommodity:" + seckillActivity.getCommodityId());
        if (commodityInfo == null || commodityInfo.equals("")) {
            seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        }
        else {
            // Seckill commodity has been stored into Redis Cache.
            // Retrieving from cache is much faster than retrieving from database.
            log.info("Seckill commodity info retrieved from Redis Cache: " + commodityInfo);
            seckillCommodity = JSON.parseObject(commodityInfo, SeckillCommodity.class);
        }

        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";
    }

    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException {
        // startTime and endTime are initially of the format YYYY-MM-DDThh:mm
        startTime = startTime.substring(0, 10) + startTime.substring(11);
        endTime = endTime.substring(0, 10) + endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");

        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));

        // add a new record (seckill promotion activity) to db
        seckillActivityDao.insertSeckillActivity(seckillActivity);

        // resultMap is used by add_success.html to render some info of the newly created seckill activity
        resultMap.put("seckillActivity", seckillActivity);

        return "add_success";
    }

    /**
     * Process seckill request
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/seckill/purchase/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId, @PathVariable long seckillActivityId) {
        boolean stockValidationResult = false;
        ModelAndView modelAndView = new ModelAndView();

        try {
            stockValidationResult = seckillActivityService.validateStock(seckillActivityId);
            if (stockValidationResult) {
                Order order = seckillActivityService.createOrder(seckillActivityId, userId);
                modelAndView.addObject("resultInfo","Seckill succeeds! " +
                        "Creating the order，order ID：" + order.getOrderNo());
                modelAndView.addObject("orderNo", order.getOrderNo());
            }
            else {
                modelAndView.addObject("resultInfo","Sorry，there is no available stock.");
            }
        } catch (Exception e) {
            log.error("Seckill System throws exception: " + e.toString());
            modelAndView.addObject("resultInfo","Seckill fails.");
        }

        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    /**
     * Get order info
     * @param orderNo
     * @return
     */
    @RequestMapping("/seckill/orderInfo/{orderNo}")
    public ModelAndView orderInfo(@PathVariable String orderNo) {
        Order order = orderDao.queryOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (order != null) {
            modelAndView.setViewName("order_info");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        }
        else {
            modelAndView.setViewName("order_wait");
        }

        return modelAndView;
    }

    /**
     * Order checkout
     * @return
     */
    @RequestMapping("/seckill/checkout/{orderNo}")
    public String orderCheckout(@PathVariable String orderNo) throws Exception {
        seckillActivityService.checkoutProcess(orderNo);
        return "redirect:/seckill/orderInfo/" + orderNo;
    }
}
