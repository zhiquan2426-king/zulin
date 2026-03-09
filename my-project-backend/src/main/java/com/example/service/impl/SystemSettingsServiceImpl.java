package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.SystemSettings;
import com.example.mapper.SystemSettingsMapper;
import com.example.service.SystemSettingsService;
import com.example.entity.vo.request.SystemSettingsUpdateVO;
import com.example.entity.vo.response.SystemSettingsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemSettingsServiceImpl extends ServiceImpl<SystemSettingsMapper, SystemSettings> implements SystemSettingsService {

    @Resource
    private SystemSettingsMapper systemSettingsMapper;

    @Override
    public List<SystemSettingsVO> getAllSettings() {
        return systemSettingsMapper.selectList(new QueryWrapper<>()).stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public SystemSettingsVO getSettingByKey(String settingKey) {
        SystemSettings settings = systemSettingsMapper.selectOne(
                new QueryWrapper<SystemSettings>().eq("setting_key", settingKey));
        return convertToVO(settings);
    }

    @Override
    public boolean updateSetting(SystemSettingsUpdateVO vo) {
        SystemSettings settings = systemSettingsMapper.selectOne(
                new QueryWrapper<SystemSettings>().eq("setting_key", vo.getSettingKey()));
        if (settings == null) {
            throw new RuntimeException("设置不存在");
        }
        settings.setSettingValue(vo.getSettingValue());
        return updateById(settings);
    }

    private SystemSettingsVO convertToVO(SystemSettings settings) {
        if (settings == null) {
            return null;
        }
        SystemSettingsVO vo = new SystemSettingsVO();
        vo.setId(settings.getId());
        vo.setSettingKey(settings.getSettingKey());
        vo.setSettingValue(settings.getSettingValue());
        vo.setDescription(settings.getDescription());
        vo.setCreateTime(settings.getCreateTime());
        vo.setUpdateTime(settings.getUpdateTime());
        return vo;
    }
}
