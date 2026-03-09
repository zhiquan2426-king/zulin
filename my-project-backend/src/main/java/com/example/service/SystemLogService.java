package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.SystemLog;
import com.example.entity.vo.request.SystemLogQueryVO;
import com.example.entity.vo.response.SystemLogVO;

public interface SystemLogService {
    
    IPage<SystemLogVO> getLogPage(SystemLogQueryVO query);
    
    boolean saveLog(SystemLog log);
    
    void clearLogs();
}
