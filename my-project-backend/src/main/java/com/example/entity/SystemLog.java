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
@TableName("db_system_log")
public class SystemLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private Long time;
    private String ip;
    private Integer status;  // 0-失败，1-成功
    private String error;
    private LocalDateTime createTime;
}
