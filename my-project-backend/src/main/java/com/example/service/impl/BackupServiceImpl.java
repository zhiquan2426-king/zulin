package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Backup;
import com.example.mapper.BackupMapper;
import com.example.service.BackupService;
import com.example.entity.vo.response.BackupVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BackupServiceImpl extends ServiceImpl<BackupMapper, Backup> implements BackupService {

    @Resource
    private BackupMapper backupMapper;

    @Override
    public IPage<BackupVO> getBackupPage(Page<?> page) {
        return backupMapper.selectBackupPage(page);
    }

    @Override
    public boolean createBackup(Backup backup, Integer creatorId) {
        backup.setCreatorId(creatorId);
        return save(backup);
    }

    @Override
    public boolean deleteBackup(Integer id) {
        return removeById(id);
    }
}
