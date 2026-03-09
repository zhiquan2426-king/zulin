package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.vo.request.EvaluationAddVO;
import com.example.entity.vo.response.EvaluationVO;

public interface EvaluationService {

    IPage<EvaluationVO> getEvaluationPage(Page<?> page, Integer equipmentId);

    EvaluationVO getEvaluationById(Integer id);

    EvaluationVO getEvaluationByOrderId(Integer orderId);

    boolean addEvaluation(EvaluationAddVO vo, Integer userId);

    boolean deleteEvaluation(Integer id, Integer userId);
}
