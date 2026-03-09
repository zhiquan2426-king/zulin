package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Courier;
import com.example.entity.Delivery;
import com.example.entity.DeliveryTracking;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.DeliveryCreateVO;
import com.example.entity.vo.response.DeliveryVO;
import com.example.mapper.CourierMapper;
import com.example.mapper.DeliveryMapper;
import com.example.mapper.DeliveryTrackingMapper;
import com.example.service.DeliveryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl extends ServiceImpl<DeliveryMapper, Delivery> implements DeliveryService {

    @Resource
    private DeliveryMapper deliveryMapper;

    @Resource
    private DeliveryTrackingMapper deliveryTrackingMapper;

    @Resource
    private CourierMapper courierMapper;

    @Override
    @Transactional
    public String createDelivery(DeliveryCreateVO vo, Account account) {
        // 验证配送类型
        if (vo.getDeliveryType() == 2 && (vo.getRecipientAddress() == null || vo.getRecipientAddress().isEmpty())) {
            throw new RuntimeException("配送到家需要填写收货地址");
        }

        // 验证上门取件信息
        if (vo.getDeliveryType() == 3 && (vo.getPickupAddress() == null || vo.getPickupAddress().isEmpty())) {
            throw new RuntimeException("上门取件需要填写取件地址");
        }

        // 生成配送单号
        String deliveryNo = "DLV" + System.currentTimeMillis() + (int) (Math.random() * 1000);

        // 创建配送单
        Delivery delivery = new Delivery();
        delivery.setDeliveryNo(deliveryNo);
        delivery.setOrderNo(vo.getOrderNo());
        delivery.setDeliveryType(vo.getDeliveryType());
        delivery.setDeliveryStatus(1); // 待配送
        delivery.setRecipientName(vo.getRecipientName());
        delivery.setRecipientPhone(vo.getRecipientPhone());
        delivery.setRecipientAddress(vo.getRecipientAddress());
        delivery.setContactName(vo.getContactName());
        delivery.setContactPhone(vo.getContactPhone());
        delivery.setPickupAddress(vo.getPickupAddress());

        // 计算配送费用（这里简化处理，实际应该根据距离计算）
        BigDecimal distance = BigDecimal.ZERO;
        if (vo.getDeliveryType() == 2 || vo.getDeliveryType() == 3) {
            // 假设平均距离5公里，实际应该调用地图API计算
            distance = new BigDecimal("5");
        }
        delivery.setDistance(distance);

        BigDecimal deliveryFee = calculateDeliveryFee(vo.getDeliveryType(), distance);
        delivery.setDeliveryFee(deliveryFee);

        // 估算配送时间
        delivery.setEstimatedTime(30); // 30分钟

        delivery.setCreateTime(LocalDateTime.now());
        delivery.setUpdateTime(LocalDateTime.now());

        if (save(delivery)) {
            // 添加初始轨迹
            String description = vo.getDeliveryType() == 2 ? "等待配送员上门" : "等待上门取件";
            addTracking(deliveryNo, 1, "仓库", description, "系统");
            return deliveryNo;
        }
        return null;
    }

    @Override
    public IPage<DeliveryVO> getDeliveryList(Integer page, Integer size, Integer status, String orderNo, Account account) {
        Page<DeliveryVO> pageObj = new Page<>(page, size);
        // 普通用户只能看到自己的配送，管理员可以看到所有
        Integer userId = account.getRole().equals("system_admin") ? null : account.getId();
        return deliveryMapper.selectDeliveryPage(pageObj, userId, status, orderNo);
    }

    @Override
    public DeliveryVO getDeliveryByNo(String deliveryNo) {
        return deliveryMapper.selectDeliveryByNo(deliveryNo);
    }

    @Override
    @Transactional
    public boolean updateDeliveryStatus(String deliveryNo, Integer status, String location, Account account) {
        Delivery delivery = deliveryMapper.selectOne(
                new QueryWrapper<Delivery>().eq("delivery_no", deliveryNo)
        );

        if (delivery == null) {
            throw new RuntimeException("配送单不存在");
        }

        // 更新状态
        delivery.setDeliveryStatus(status);

        // 更新配送员信息
        if (account.getRole().equals("courier")) {
            if (delivery.getCourierId() == null) {
                delivery.setCourierId(account.getId());
                delivery.setCourierName(account.getUsername());
                delivery.setCourierPhone(account.getEmail()); // 假设email存储的是电话
            }
        }

        // 更新时间
        if (status == 2) { // 配送中
            delivery.setStartTime(LocalDateTime.now());
        } else if (status == 3 || status == 5) { // 已签收或已完成
            delivery.setCompleteTime(LocalDateTime.now());
            if (delivery.getStartTime() != null) {
                long minutes = java.time.Duration.between(delivery.getStartTime(), LocalDateTime.now()).toMinutes();
                delivery.setActualTime((int) minutes);
            }
        }

        delivery.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(delivery);

        // 添加轨迹
        if (updated) {
            String description = getStatusText(status);
            String loc = location != null ? location : (delivery.getRecipientAddress() != null ? delivery.getRecipientAddress() : "仓库");
            addTracking(deliveryNo, status, loc, description, account.getUsername());

            // 如果完成配送，更新配送员统计
            if ((status == 3 || status == 5) && delivery.getCourierId() != null) {
                Courier courier = courierMapper.selectById(delivery.getCourierId());
                if (courier != null) {
                    courier.setTotalDeliveries(courier.getTotalDeliveries() + 1);
                    courier.setTotalDistance(courier.getTotalDistance().add(delivery.getDistance()));
                    courierMapper.updateById(courier);
                }
            }
        }

        return updated;
    }

    @Override
    @Transactional
    public boolean assignCourier(String deliveryNo, Integer courierId) {
        Delivery delivery = deliveryMapper.selectOne(
                new QueryWrapper<Delivery>().eq("delivery_no", deliveryNo)
        );

        if (delivery == null) {
            throw new RuntimeException("配送单不存在");
        }

        Courier courier = courierMapper.selectById(courierId);
        if (courier == null || courier.getStatus() == 0) {
            throw new RuntimeException("配送员不存在或已禁用");
        }

        delivery.setCourierId(courierId);
        delivery.setCourierName(courier.getName());
        delivery.setCourierPhone(courier.getPhone());
        delivery.setDeliveryStatus(2); // 配送中
        delivery.setStartTime(LocalDateTime.now());
        delivery.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(delivery);

        if (updated) {
            addTracking(deliveryNo, 2, "仓库", "配送员已接单，准备取件", courier.getName());

            // 更新配送员状态为忙碌
            courier.setStatus(2);
            courierMapper.updateById(courier);
        }

        return updated;
    }

    @Override
    @Transactional
    public boolean cancelDelivery(String deliveryNo, Account account) {
        Delivery delivery = deliveryMapper.selectOne(
                new QueryWrapper<Delivery>().eq("delivery_no", deliveryNo)
        );

        if (delivery == null) {
            throw new RuntimeException("配送单不存在");
        }

        // 只有待配送状态可以取消
        if (!delivery.getDeliveryStatus().equals(1)) {
            throw new RuntimeException("当前状态不允许取消");
        }

        delivery.setDeliveryStatus(6); // 已取消
        delivery.setUpdateTime(LocalDateTime.now());

        return updateById(delivery);
    }

    @Override
    public List<Map<String, Object>> getAvailableCouriers() {
        List<Courier> couriers = courierMapper.selectList(
                new QueryWrapper<Courier>()
                        .eq("status", 1) // 空闲状态
                        .orderByDesc("rating")
                        .orderByAsc("total_deliveries")
                        .last("LIMIT 10")
        );

        return couriers.stream().map(courier -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", courier.getId());
            map.put("name", courier.getName());
            map.put("phone", courier.getPhone());
            map.put("vehicleType", courier.getVehicleType());
            map.put("rating", courier.getRating());
            map.put("totalDeliveries", courier.getTotalDeliveries());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDeliveryTracking(String deliveryNo) {
        List<DeliveryTracking> trackings = deliveryTrackingMapper.selectList(
                new QueryWrapper<DeliveryTracking>()
                        .eq("delivery_no", deliveryNo)
                        .orderByAsc("create_time")
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return trackings.stream().map(tracking -> {
            Map<String, Object> map = new HashMap<>();
            map.put("status", tracking.getTrackingStatus());
            map.put("statusText", getTrackingStatusText(tracking.getTrackingStatus()));
            map.put("location", tracking.getLocation());
            map.put("description", tracking.getDescription());
            map.put("operator", tracking.getOperator());
            map.put("time", tracking.getCreateTime().format(formatter));
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateDeliveryFee(Integer deliveryType, BigDecimal distance) {
        // 自提免费
        if (deliveryType == 1) {
            return BigDecimal.ZERO;
        }

        // 根据距离计算费用（简化版，实际应该从数据库读取费率表）
        if (distance.compareTo(new BigDecimal("5")) <= 0) {
            return deliveryType == 2 ? new BigDecimal("10.00") : new BigDecimal("8.00");
        } else if (distance.compareTo(new BigDecimal("10")) <= 0) {
            return deliveryType == 2 ? new BigDecimal("18.00") : new BigDecimal("14.00");
        } else {
            return deliveryType == 2 ? new BigDecimal("25.00") : new BigDecimal("20.00");
        }
    }

    @Override
    @Transactional
    public boolean startDelivery(String deliveryNo, Account account) {
        // 验证是否为配送员或系统管理员
        if (!account.getRole().equals("courier") && !account.getRole().equals("system_admin")) {
            throw new RuntimeException("只有配送员或系统管理员可以开始配送");
        }

        Delivery delivery = deliveryMapper.selectOne(
                new QueryWrapper<Delivery>().eq("delivery_no", deliveryNo)
        );

        if (delivery == null) {
            throw new RuntimeException("配送单不存在");
        }

        // 只有配送中状态可以开始配送
        if (!delivery.getDeliveryStatus().equals(2)) {
            throw new RuntimeException("当前状态不允许开始配送");
        }

        // 如果是配送员，验证是否为该配送员的订单
        if (account.getRole().equals("courier") && !delivery.getCourierId().equals(account.getId())) {
            throw new RuntimeException("这不是您的配送单");
        }

        delivery.setDeliveryStatus(2); // 配送中
        delivery.setStartTime(LocalDateTime.now());
        delivery.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(delivery);

        if (updated) {
            addTracking(deliveryNo, 2, "仓库", "配送已开始，准备出发", account.getUsername());
        }

        return updated;
    }

    @Override
    @Transactional
    public boolean completeDelivery(String deliveryNo, Account account) {
        // 验证是否为配送员或系统管理员
        if (!account.getRole().equals("courier") && !account.getRole().equals("system_admin")) {
            throw new RuntimeException("只有配送员或系统管理员可以完成配送");
        }

        Delivery delivery = deliveryMapper.selectOne(
                new QueryWrapper<Delivery>().eq("delivery_no", deliveryNo)
        );

        if (delivery == null) {
            throw new RuntimeException("配送单不存在");
        }

        // 只有配送中状态可以完成配送
        if (!delivery.getDeliveryStatus().equals(2)) {
            throw new RuntimeException("当前状态不允许完成配送");
        }

        // 如果是配送员，验证是否为该配送员的订单
        if (account.getRole().equals("courier") && !delivery.getCourierId().equals(account.getId())) {
            throw new RuntimeException("这不是您的配送单");
        }

        delivery.setDeliveryStatus(5); // 已完成
        delivery.setCompleteTime(LocalDateTime.now());
        delivery.setUpdateTime(LocalDateTime.now());

        // 计算实际用时
        if (delivery.getStartTime() != null) {
            long minutes = java.time.Duration.between(delivery.getStartTime(), LocalDateTime.now()).toMinutes();
            delivery.setActualTime((int) minutes);
        }

        boolean updated = updateById(delivery);

        if (updated) {
            String location = delivery.getRecipientAddress() != null ? delivery.getRecipientAddress() : "目的地";
            addTracking(deliveryNo, 5, location, "配送已完成", account.getUsername());

            // 更新配送员统计
            if (delivery.getCourierId() != null) {
                Courier courier = courierMapper.selectById(delivery.getCourierId());
                if (courier != null) {
                    courier.setTotalDeliveries(courier.getTotalDeliveries() + 1);
                    courier.setTotalDistance(courier.getTotalDistance().add(delivery.getDistance()));
                    courier.setStatus(1); // 设置为空闲
                    courierMapper.updateById(courier);
                }
            }
        }

        return updated;
    }

    /**
     * 添加配送轨迹
     */
    private void addTracking(String deliveryNo, Integer status, String location, String description, String operator) {
        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setDeliveryNo(deliveryNo);
        tracking.setTrackingStatus(status);
        tracking.setLocation(location);
        tracking.setDescription(description);
        tracking.setOperator(operator);
        tracking.setCreateTime(LocalDateTime.now());
        deliveryTrackingMapper.insert(tracking);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1: return "等待配送";
            case 2: return "配送员已接单";
            case 3: return "已签收";
            case 4: return "运输中";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知状态";
        }
    }

    /**
     * 获取轨迹状态文本
     */
    private String getTrackingStatusText(Integer status) {
        switch (status) {
            case 1: return "待取件";
            case 2: return "已取件";
            case 3: return "运输中";
            case 4: return "派送中";
            case 5: return "已签收";
            case 6: return "异常";
            default: return "未知状态";
        }
    }
}
