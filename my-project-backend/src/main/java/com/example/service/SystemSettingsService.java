package com.example.service;

import com.example.entity.vo.request.SystemSettingsUpdateVO;
import com.example.entity.vo.response.SystemSettingsVO;

import java.util.List;

public interface SystemSettingsService {
    
    List<SystemSettingsVO> getAllSettings();
    
    SystemSettingsVO getSettingByKey(String settingKey);
    
    boolean updateSetting(SystemSettingsUpdateVO vo);
}
