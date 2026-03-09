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
@TableName("db_system_settings")
public class SystemSettings {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
