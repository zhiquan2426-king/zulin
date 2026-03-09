package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer equipmentId;
    private Integer quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rentalDays;
    private BigDecimal dailyPrice;
    private BigDecimal totalAmount;
    private BigDecimal deposit;
    private BigDecimal payableAmount;
    private String status;  // pending-待支付，paid-已支付，renting-租赁中，completed-已完成，cancelled-已取消，overdue-已逾期
    private String paymentStatus;  // unpaid-未支付，paid-已支付，refunded-已退款
    private String depositDeductionStatus;  // none-无扣除，pending-待扣除，deducted-已扣除，rejected-已拒绝
    private java.math.BigDecimal deductedAmount;  // 已扣除押金金额
    private LocalDateTime paymentTime;
    private LocalDateTime returnTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
}
