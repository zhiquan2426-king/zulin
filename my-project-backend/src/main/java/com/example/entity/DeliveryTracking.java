package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_delivery_tracking")
public class DeliveryTracking {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String deliveryNo;
    private Integer trackingStatus;  // 1-待取件，2-已取件，3-运输中，4-派送中，5-已签收，6-异常
    private String location;
    private String description;
    private String operator;
    private LocalDateTime createTime;
}
