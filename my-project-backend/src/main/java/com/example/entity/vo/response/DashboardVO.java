package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    // 统计数据
    private Integer totalUsers;           // 总用户数
    private Integer totalEquipment;       // 总设备数
    private Integer totalOrders;          // 总订单数
    private Integer activeRentals;        // 活跃租赁数
    private BigDecimal totalRevenue;      // 总收入
    private BigDecimal todayRevenue;      // 今日收入
    
    // 趋势数据
    private Integer todayOrders;          // 今日订单数
    private Integer todayNewUsers;        // 今日新增用户
    
    // 设备统计
    private Integer lowStockEquipment;    // 库存不足的设备数量
    private Integer overdueOrders;        // 逾期订单数量
}
