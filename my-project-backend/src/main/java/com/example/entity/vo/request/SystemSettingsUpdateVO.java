package com.example.entity.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemSettingsUpdateVO {
    @NotBlank(message = "设置键不能为空")
    private String settingKey;
    
    private String settingValue;
}
