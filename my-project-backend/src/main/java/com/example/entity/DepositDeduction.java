package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_deposit_deduction")
public class DepositDeduction {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private String orderNo;
    private Integer userId;
    private Integer equipmentId;
    private String deductionType;  // damage-设备损坏，overdue-逾期归还
    private BigDecimal deductionAmount;
    private String deductionReason;
    private String damageImages;
    private String damageLevel;  // light-轻微，moderate-中度，severe-严重
    private Integer overdueDays;
    private String status;  // pending-待处理，approved-已批准，rejected-已拒绝
    private Integer adminId;
    private String adminName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime handleTime;
}
