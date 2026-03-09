package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.vo.request.DepositDeductionCreateVO;
import com.example.entity.vo.response.DepositDeductionVO;

public interface DepositDeductionService {
    
    IPage<DepositDeductionVO> getUserDeductionPage(Integer userId, String status, Integer page, Integer size);
    
    IPage<DepositDeductionVO> getAllDeductionPage(String status, Integer page, Integer size);
    
    DepositDeductionVO getDeductionById(Integer id);
    
    boolean createDeduction(DepositDeductionCreateVO vo, Integer adminId);
    
    boolean approveDeduction(Integer id, Integer adminId);
    
    boolean rejectDeduction(Integer id, Integer adminId);
}
