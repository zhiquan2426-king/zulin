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
@TableName("db_evaluation")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer userId;
    private Integer equipmentId;
    private Integer rating;  // 1-5星
    private String content;
    private String images;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;  // 0-隐藏，1-显示
}
