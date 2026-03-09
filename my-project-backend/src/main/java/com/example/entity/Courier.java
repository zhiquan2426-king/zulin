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
@TableName("db_courier")
public class Courier {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String courierNo;
    private String name;
    private String phone;
    private String idCard;
    private String vehicleType;
    private String vehiclePlate;
    private Integer status;  // 1-空闲，2-忙碌，3-休息，0-禁用
    private Integer totalDeliveries;
    private BigDecimal totalDistance;
    private BigDecimal rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
