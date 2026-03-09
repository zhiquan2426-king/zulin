package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.vo.response.DepositDeductionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepositDeductionMapper extends BaseMapper<com.example.entity.DepositDeduction> {
    
    IPage<DepositDeductionVO> selectDeductionPage(Page<DepositDeductionVO> page, @Param("userId") Integer userId, @Param("status") String status);
    
    DepositDeductionVO selectDeductionById(@Param("id") Integer id);
}
