package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Backup;
import com.example.entity.vo.response.BackupVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupMapper extends BaseMapper<Backup> {
    
    IPage<BackupVO> selectBackupPage(Page<?> page);
}
