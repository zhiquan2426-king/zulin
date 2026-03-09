package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Evaluation;
import com.example.entity.vo.response.EvaluationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    IPage<EvaluationVO> selectEvaluationPage(Page<?> page, @Param("equipmentId") Integer equipmentId);

    EvaluationVO selectEvaluationById(Integer id);

    EvaluationVO selectEvaluationByOrderId(Integer orderId);
}
