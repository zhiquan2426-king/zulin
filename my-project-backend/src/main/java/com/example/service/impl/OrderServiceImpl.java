package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Equipment;
import com.example.entity.Order;
import com.example.entity.SystemSettings;
import com.example.mapper.EquipmentMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.SystemSettingsMapper;
import com.example.service.DeliveryService;
import com.example.service.EquipmentService;
import com.example.service.OrderService;
import com.example.entity.vo.request.DeliveryCreateVO;
import com.example.entity.vo.request.OrderCreateVO;
import com.example.entity.vo.request.OrderQueryVO;
import com.example.entity.vo.response.OrderVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private EquipmentMapper equipmentMapper;
    
    @Resource
    private EquipmentService equipmentService;
    
    @Resource
    private SystemSettingsMapper systemSettingsMapper;

    @Resource
    private DeliveryService deliveryService;

    @Override
    public IPage<OrderVO> getUserOrderPage(Integer userId, OrderQueryVO query) {
        Page<OrderVO> page = new Page<>(query.getPage(), query.getSize());
        return orderMapper.selectOrderPage(page, userId, query);
    }

    @Override
    public IPage<OrderVO> getAllOrderPage(OrderQueryVO query) {
        Page<OrderVO> page = new Page<>(query.getPage(), query.getSize());
        return orderMapper.selectOrderPage(page, null, query);
    }

    @Override
    public OrderVO getOrderById(Integer id) {
        return orderMapper.selectOrderById(id);
    }

    @Override
    public OrderVO getOrderNo(String orderNo) {
        return orderMapper.selectOrderByNo(orderNo);
    }

    @Override
    @Transactional
    public boolean createOrder(OrderCreateVO vo, Integer userId) {
        // 验证日期
        if (vo.getStartDate().isAfter(vo.getEndDate())) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }

        // 计算租赁天数
        long days = ChronoUnit.DAYS.between(vo.getStartDate(), vo.getEndDate()) + 1;

        // 获取系统设置的最大租赁天数
        SystemSettings maxDaysSetting = systemSettingsMapper.selectById(5);
        int maxDays = maxDaysSetting != null ? Integer.parseInt(maxDaysSetting.getSettingValue()) : 30;

        if (days > maxDays) {
            throw new RuntimeException("租赁天数不能超过" + maxDays + "天");
        }

        // 查询设备
        Equipment equipment = equipmentMapper.selectById(vo.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        if (equipment.getStatus() == 0) {
            throw new RuntimeException("该设备已下架");
        }

        if (equipment.getAvailable() < vo.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        // 检查日期冲突
        long conflictCount = orderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .eq("equipment_id", vo.getEquipmentId())
                        .in("status", "paid", "renting", "overdue")
                        .and(wrapper -> wrapper
                                .and(w -> w.le("start_date", vo.getEndDate()).ge("end_date", vo.getStartDate()))
                                .or()
                                .and(w -> w.ge("start_date", vo.getStartDate()).le("end_date", vo.getEndDate()))
                        )
        );

        if (conflictCount > 0) {
            throw new RuntimeException("所选日期已被预订");
        }

        // 计算金额
        BigDecimal dailyPrice = equipment.getDailyPrice();
        BigDecimal totalAmount = dailyPrice.multiply(BigDecimal.valueOf(days * vo.getQuantity()));
        BigDecimal deposit = equipment.getDeposit().multiply(BigDecimal.valueOf(vo.getQuantity()));

        // 计算配送费用
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (vo.getDeliveryType() != null && vo.getDeliveryType() != 1) {
            // 配送到家或上门取件，基础费用20元
            deliveryFee = new BigDecimal("20");
        }

        BigDecimal payableAmount = totalAmount.add(deposit).add(deliveryFee);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setEquipmentId(vo.getEquipmentId());
        order.setQuantity(vo.getQuantity());
        order.setStartDate(vo.getStartDate());
        order.setEndDate(vo.getEndDate());
        order.setRentalDays((int) days);
        order.setDailyPrice(dailyPrice);
        order.setTotalAmount(totalAmount);
        order.setDeposit(deposit);
        order.setPayableAmount(payableAmount);
        order.setStatus("pending");
        order.setPaymentStatus("unpaid");
        order.setRemark(vo.getRemark());

        boolean orderSaved = save(order);

        // 如果选择了配送方式，创建配送单
        if (orderSaved && vo.getDeliveryType() != null && vo.getDeliveryType() != 1) {
            DeliveryCreateVO deliveryVO = new DeliveryCreateVO();
            deliveryVO.setOrderNo(order.getOrderNo());
            deliveryVO.setDeliveryType(vo.getDeliveryType());
            deliveryVO.setRecipientName(vo.getRecipientName());
            deliveryVO.setRecipientPhone(vo.getRecipientPhone());
            deliveryVO.setRecipientAddress(vo.getRecipientAddress());
            deliveryVO.setContactName(vo.getContactName());
            deliveryVO.setContactPhone(vo.getContactPhone());
            deliveryVO.setPickupAddress(vo.getPickupAddress());

            try {
                String deliveryNo = deliveryService.createDelivery(deliveryVO, null);
                // 更新订单的配送单号
                if (deliveryNo != null) {
                    orderMapper.updateDeliveryNo(order.getId(), deliveryNo);
                }
            } catch (Exception e) {
                // 配送单创建失败，不影响订单创建
                System.err.println("创建配送单失败: " + e.getMessage());
            }
        }

        return orderSaved;
    }

    @Override
    @Transactional
    public boolean payOrder(Integer orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getStatus().equals("pending")) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus("paid");
        order.setPaymentStatus("paid");
        order.setPaymentTime(LocalDateTime.now());
        
        boolean updated = updateById(order);
        if (updated) {
            equipmentService.updateAvailable(order.getEquipmentId(), -order.getQuantity());
        }
        
        return updated;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Integer orderId, Integer userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该订单");
        }
        
        if (!order.getStatus().equals("pending")) {
            throw new RuntimeException("订单状态不正确，无法取消");
        }
        
        order.setStatus("cancelled");
        return updateById(order);
    }

    @Override
    @Transactional
    public boolean completeOrder(Integer orderId, Integer adminId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getStatus().equals("paid")) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus("renting");
        return updateById(order);
    }

    @Override
    @Transactional
    public boolean returnEquipment(Integer orderId, Integer adminId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getStatus().equals("renting") && !order.getStatus().equals("overdue")) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus("completed");
        order.setPaymentStatus("refunded");
        order.setReturnTime(LocalDateTime.now());
        
        boolean updated = updateById(order);
        if (updated) {
            equipmentService.updateAvailable(order.getEquipmentId(), order.getQuantity());
        }
        
        return updated;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
