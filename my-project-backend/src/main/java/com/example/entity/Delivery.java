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
@TableName("db_delivery")
public class Delivery {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private String deliveryNo;
    private Integer deliveryType;  // 1-自提，2-配送到家，3-上门取件
    private Integer deliveryStatus;  // 1-待配送，2-配送中，3-已签收，4-运输中，5-已完成，6-已取消
    private Integer courierId;
    private String courierName;
    private String courierPhone;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String contactName;
    private String contactPhone;
    private String pickupAddress;
    private BigDecimal deliveryFee;
    private BigDecimal distance;
    private Integer estimatedTime;
    private Integer actualTime;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
