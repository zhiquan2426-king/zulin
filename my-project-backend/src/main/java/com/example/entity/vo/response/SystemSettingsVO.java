package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsVO {
    private Integer id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
