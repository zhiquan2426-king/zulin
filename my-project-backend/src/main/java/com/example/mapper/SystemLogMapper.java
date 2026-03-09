package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.SystemLog;
import com.example.entity.vo.request.SystemLogQueryVO;
import com.example.entity.vo.response.SystemLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {
    
    IPage<SystemLogVO> selectLogPage(Page<?> page, @Param("query") SystemLogQueryVO query);
}
