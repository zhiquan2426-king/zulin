package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SystemSettings;
import com.example.entity.vo.response.SystemSettingsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemSettingsMapper extends BaseMapper<SystemSettings> {
    
    @Select("SELECT * FROM db_system_settings WHERE setting_key = #{settingKey}")
    SystemSettingsVO selectByKey(@Param("settingKey") String settingKey);

    @Select("SELECT setting_value FROM db_system_settings WHERE setting_key = #{settingKey}")
    String selectBySettingKey(@Param("settingKey") String settingKey);
}
