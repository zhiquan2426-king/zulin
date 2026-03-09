package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.DepositDeduction;
import com.example.entity.Order;
import com.example.entity.dto.Account;
import com.example.mapper.DepositDeductionMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.SystemSettingsMapper;
import com.example.service.AccountService;
import com.example.service.DepositDeductionService;
import com.example.entity.vo.request.DepositDeductionCreateVO;
import com.example.entity.vo.response.DepositDeductionVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DepositDeductionServiceImpl extends ServiceImpl<DepositDeductionMapper, DepositDeduction>
        implements DepositDeductionService {

    @Resource
    private DepositDeductionMapper deductionMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private SystemSettingsMapper systemSettingsMapper;

    @Resource
    private AccountService accountService;

    @Override
    public IPage<DepositDeductionVO> getUserDeductionPage(Integer userId, String status, Integer page, Integer size) {
        Page<DepositDeductionVO> pagination = new Page<>(page, size);
        return deductionMapper.selectDeductionPage(pagination, userId, status);
    }

    @Override
    public IPage<DepositDeductionVO> getAllDeductionPage(String status, Integer page, Integer size) {
        Page<DepositDeductionVO> pagination = new Page<>(page, size);
        return deductionMapper.selectDeductionPage(pagination, null, status);
    }

    @Override
    public DepositDeductionVO getDeductionById(Integer id) {
        return deductionMapper.selectDeductionById(id);
    }

    @Override
    @Transactional
    public boolean createDeduction(DepositDeductionCreateVO vo, Integer adminId) {
        // 验证订单
        Order order = orderMapper.selectById(vo.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证扣除金额不能超过押金
        if (vo.getDeductionAmount().compareTo(order.getDeposit()) > 0) {
            throw new RuntimeException("扣除金额不能超过押金金额");
        }

        // 自动计算扣款金额（如果未手动指定或需要验证）
        if ("overdue".equals(vo.getDeductionType()) && vo.getOverdueDays() != null) {
            // 查询逾期每日扣款比例
            String rateStr = systemSettingsMapper.selectBySettingKey("overdue_daily_penalty_rate");
            BigDecimal rate = new BigDecimal(rateStr != null ? rateStr : "0.1");
            BigDecimal autoAmount = order.getDeposit().multiply(rate).multiply(new BigDecimal(vo.getOverdueDays()));
            
            // 如果计算金额超过押金，取押金金额
            autoAmount = autoAmount.min(order.getDeposit());
            
            // 可以在这里验证或更新扣款金额
            if (vo.getDeductionAmount().compareTo(autoAmount) > 0) {
                throw new RuntimeException("逾期扣款金额计算错误，应不超过：" + autoAmount);
            }
        }

        if ("damage".equals(vo.getDeductionType()) && vo.getDamageLevel() != null) {
            String rateStr = null;
            switch (vo.getDamageLevel()) {
                case "light":
                    rateStr = systemSettingsMapper.selectBySettingKey("light_damage_deduction_rate");
                    break;
                case "moderate":
                    rateStr = systemSettingsMapper.selectBySettingKey("moderate_damage_deduction_rate");
                    break;
                case "severe":
                    rateStr = systemSettingsMapper.selectBySettingKey("severe_damage_deduction_rate");
                    break;
            }
            if (rateStr != null) {
                BigDecimal rate = new BigDecimal(rateStr);
                BigDecimal autoAmount = order.getDeposit().multiply(rate);
                if (vo.getDeductionAmount().compareTo(autoAmount) > 0) {
                    throw new RuntimeException("损坏扣款金额计算错误，应不超过：" + autoAmount);
                }
            }
        }

        // 获取管理员信息
        Account admin = accountService.findAccountById(adminId);

        // 创建扣除记录
        DepositDeduction deduction = new DepositDeduction();
        deduction.setOrderId(vo.getOrderId());
        deduction.setOrderNo(order.getOrderNo());
        deduction.setUserId(order.getUserId());
        deduction.setEquipmentId(order.getEquipmentId());
        deduction.setDeductionType(vo.getDeductionType());
        deduction.setDeductionAmount(vo.getDeductionAmount());
        deduction.setDeductionReason(vo.getDeductionReason());
        deduction.setDamageImages(vo.getDamageImages());
        deduction.setDamageLevel(vo.getDamageLevel());
        deduction.setOverdueDays(vo.getOverdueDays());
        deduction.setStatus("pending");
        deduction.setAdminId(adminId);
        deduction.setAdminName(admin != null ? admin.getUsername() : "");
        deduction.setCreateTime(LocalDateTime.now());

        boolean saved = save(deduction);

        // 更新订单的押金扣除状态
        if (saved) {
            orderMapper.updateDepositDeductionStatus(vo.getOrderId(), "pending", vo.getDeductionAmount());
        }

        return saved;
    }

    @Override
    @Transactional
    public boolean approveDeduction(Integer id, Integer adminId) {
        DepositDeduction deduction = getById(id);
        if (deduction == null) {
            throw new RuntimeException("扣除记录不存在");
        }

        if (!deduction.getStatus().equals("pending")) {
            throw new RuntimeException("记录状态不正确");
        }

        // 更新扣除记录状态
        deduction.setStatus("approved");
        deduction.setHandleTime(LocalDateTime.now());
        boolean updated = updateById(deduction);

        // 更新订单的押金扣除状态
        if (updated) {
            Order order = orderMapper.selectById(deduction.getOrderId());
            if (order != null) {
                BigDecimal totalDeducted = (order.getDeductedAmount() != null ? order.getDeductedAmount() : BigDecimal.ZERO)
                        .add(deduction.getDeductionAmount());
                orderMapper.updateDepositDeductionStatus(deduction.getOrderId(), "deducted", totalDeducted);
            }
        }

        return updated;
    }

    @Override
    @Transactional
    public boolean rejectDeduction(Integer id, Integer adminId) {
        DepositDeduction deduction = getById(id);
        if (deduction == null) {
            throw new RuntimeException("扣除记录不存在");
        }

        if (!deduction.getStatus().equals("pending")) {
            throw new RuntimeException("记录状态不正确");
        }

        // 更新扣除记录状态
        deduction.setStatus("rejected");
        deduction.setHandleTime(LocalDateTime.now());
        boolean updated = updateById(deduction);

        // 检查该订单是否还有其他待处理的扣除记录
        if (updated) {
            long pendingCount = count(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DepositDeduction>()
                            .eq("order_id", deduction.getOrderId())
                            .eq("status", "pending")
            );

            // 如果没有待处理的记录，更新订单状态为已拒绝
            if (pendingCount == 0) {
                Order order = orderMapper.selectById(deduction.getOrderId());
                if (order != null && "pending".equals(order.getDepositDeductionStatus())) {
                    orderMapper.updateDepositDeductionStatus(deduction.getOrderId(), "rejected", order.getDeductedAmount());
                }
            }
        }

        return updated;
    }
}
