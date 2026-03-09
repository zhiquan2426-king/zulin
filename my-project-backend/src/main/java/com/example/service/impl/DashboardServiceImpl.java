package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.Account;
import com.example.entity.Order;
import com.example.entity.Equipment;
import com.example.mapper.AccountMapper;
import com.example.mapper.EquipmentMapper;
import com.example.mapper.OrderMapper;
import com.example.service.DashboardService;
import com.example.entity.vo.response.DashboardVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private EquipmentMapper equipmentMapper;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        // 总用户数
        vo.setTotalUsers(accountMapper.selectCount(null).intValue());

        // 总设备数
        vo.setTotalEquipment(equipmentMapper.selectCount(null).intValue());

        // 总订单数
        vo.setTotalOrders(orderMapper.selectCount(null).intValue());

        // 活跃租赁数
        vo.setActiveRentals(orderMapper.selectCount(new QueryWrapper<Order>()
                .in("status", "paid", "renting", "overdue")).intValue());

        // 总收入
        Order totalRevenueOrder = orderMapper.selectOne(new QueryWrapper<Order>()
                .select("IFNULL(SUM(total_amount), 0) as total_amount")
                .in("payment_status", "paid"));
        vo.setTotalRevenue(totalRevenueOrder != null && totalRevenueOrder.getTotalAmount() != null
                ? totalRevenueOrder.getTotalAmount() : BigDecimal.ZERO);

        // 今日收入
        vo.setTodayRevenue(getTodayRevenue());

        // 今日订单数
        vo.setTodayOrders(orderMapper.selectCount(new QueryWrapper<Order>()
                .ge("create_time", LocalDate.now().atStartOfDay())).intValue());

        // 今日新增用户
        vo.setTodayNewUsers(accountMapper.selectCount(new QueryWrapper<Account>()
                .ge("register_time", LocalDate.now().atStartOfDay())).intValue());

        // 库存不足的设备数量
        vo.setLowStockEquipment(equipmentMapper.selectCount(new QueryWrapper<Equipment>()
                .lt("available", 3)
                .eq("status", 1)).intValue());

        // 逾期订单数量
        vo.setOverdueOrders(orderMapper.selectCount(new QueryWrapper<Order>()
                .eq("status", "overdue")).intValue());

        return vo;
    }

    private BigDecimal getTodayRevenue() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();

        Order todayOrder = orderMapper.selectOne(new QueryWrapper<Order>()
                .select("IFNULL(SUM(total_amount), 0) as total_amount")
                .eq("payment_status", "paid")
                .ge("create_time", todayStart)
                .lt("create_time", todayEnd));

        return todayOrder != null && todayOrder.getTotalAmount() != null
                ? todayOrder.getTotalAmount() : BigDecimal.ZERO;
    }
}
