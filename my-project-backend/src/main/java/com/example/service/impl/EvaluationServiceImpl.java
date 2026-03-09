package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Evaluation;
import com.example.entity.Order;
import com.example.mapper.EvaluationMapper;
import com.example.mapper.OrderMapper;
import com.example.service.EvaluationService;
import com.example.entity.vo.request.EvaluationAddVO;
import com.example.entity.vo.response.EvaluationVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Resource
    private EvaluationMapper evaluationMapper;
    
    @Resource
    private OrderMapper orderMapper;

    @Override
    public IPage<EvaluationVO> getEvaluationPage(Page<?> page, Integer equipmentId) {
        return evaluationMapper.selectEvaluationPage(page, equipmentId);
    }

    @Override
    public EvaluationVO getEvaluationById(Integer id) {
        return evaluationMapper.selectEvaluationById(id);
    }

    @Override
    public EvaluationVO getEvaluationByOrderId(Integer orderId) {
        return evaluationMapper.selectEvaluationByOrderId(orderId);
    }

    @Override
    @Transactional
    public boolean addEvaluation(EvaluationAddVO vo, Integer userId) {
        // 查询订单
        Order order = orderMapper.selectById(vo.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权限评价该订单");
        }
        
        if (!order.getStatus().equals("completed")) {
            throw new RuntimeException("只有已完成的订单才能评价");
        }
        
        // 检查是否已评价
        Evaluation existing = getOne(new QueryWrapper<Evaluation>()
                .eq("order_id", vo.getOrderId()));
        if (existing != null) {
            throw new RuntimeException("该订单已评价");
        }
        
        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(vo.getOrderId());
        evaluation.setUserId(userId);
        evaluation.setEquipmentId(order.getEquipmentId());
        evaluation.setRating(vo.getRating());
        evaluation.setContent(vo.getContent());
        evaluation.setImages(vo.getImages());
        evaluation.setStatus(1);
        
        return save(evaluation);
    }

    @Override
    @Transactional
    public boolean deleteEvaluation(Integer id, Integer userId) {
        Evaluation evaluation = getById(id);
        if (evaluation == null) {
            throw new RuntimeException("评价不存在");
        }
        
        if (!evaluation.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除该评价");
        }
        
        return removeById(id);
    }
}
