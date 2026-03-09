package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.SystemLog;
import com.example.mapper.SystemLogMapper;
import com.example.service.SystemLogService;
import com.example.entity.vo.request.SystemLogQueryVO;
import com.example.entity.vo.response.SystemLogVO;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Resource
    private SystemLogMapper systemLogMapper;

    @Override
    public IPage<SystemLogVO> getLogPage(SystemLogQueryVO query) {
        Page<SystemLogVO> page = new Page<>(query.getPage(), query.getSize());
        return systemLogMapper.selectLogPage(page, query);
    }

    @Override
    public boolean saveLog(SystemLog log) {
        return save(log);
    }

    @Override
    public void clearLogs() {
        // 删除30天前的日志
        systemLogMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SystemLog>()
                .lt("create_time", java.time.LocalDateTime.now().minusDays(30)));
    }
    
    // 定时清理日志（每天凌晨2点执行）
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledClearLogs() {
        clearLogs();
    }
}
