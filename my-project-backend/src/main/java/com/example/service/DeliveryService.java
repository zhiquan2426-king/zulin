package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.DeliveryCreateVO;
import com.example.entity.vo.response.DeliveryVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DeliveryService {

    /**
     * 创建配送单
     */
    String createDelivery(DeliveryCreateVO vo, Account account);

    /**
     * 获取配送单列表
     */
    IPage<DeliveryVO> getDeliveryList(Integer page, Integer size, Integer status, String orderNo, Account account);

    /**
     * 根据配送单号获取详情
     */
    DeliveryVO getDeliveryByNo(String deliveryNo);

    /**
     * 更新配送状态
     */
    boolean updateDeliveryStatus(String deliveryNo, Integer status, String location, Account account);

    /**
     * 指派配送员
     */
    boolean assignCourier(String deliveryNo, Integer courierId);

    /**
     * 取消配送单
     */
    boolean cancelDelivery(String deliveryNo, Account account);

    /**
     * 获取可用配送员
     */
    List<Map<String, Object>> getAvailableCouriers();

    /**
     * 获取配送轨迹
     */
    List<Map<String, Object>> getDeliveryTracking(String deliveryNo);

    /**
     * 计算配送费用
     */
    BigDecimal calculateDeliveryFee(Integer deliveryType, BigDecimal distance);

    /**
     * 开始配送
     */
    boolean startDelivery(String deliveryNo, Account account);

    /**
     * 完成配送
     */
    boolean completeDelivery(String deliveryNo, Account account);
}
