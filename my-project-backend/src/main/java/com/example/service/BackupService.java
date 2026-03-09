package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Backup;
import com.example.entity.vo.response.BackupVO;

public interface BackupService {
    
    IPage<BackupVO> getBackupPage(Page<?> page);
    
    boolean createBackup(Backup backup, Integer creatorId);
    
    boolean deleteBackup(Integer id);
}
