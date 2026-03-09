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
@TableName("db_equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String category;
    private String brand;
    private String model;
    private String description;
    private BigDecimal dailyPrice;
    private BigDecimal deposit;
    private Integer stock;
    private Integer available;
    private String image;
    private Integer status;  // 0-已下架，1-上架中
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer adminId;
}
