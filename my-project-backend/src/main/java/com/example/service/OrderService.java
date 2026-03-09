package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.vo.request.OrderCreateVO;
import com.example.entity.vo.request.OrderQueryVO;
import com.example.entity.vo.response.OrderVO;

public interface OrderService {

    IPage<OrderVO> getUserOrderPage(Integer userId, OrderQueryVO query);

    IPage<OrderVO> getAllOrderPage(OrderQueryVO query);

    OrderVO getOrderById(Integer id);

    OrderVO getOrderNo(String orderNo);

    boolean createOrder(OrderCreateVO vo, Integer userId);

    boolean payOrder(Integer orderId);

    boolean cancelOrder(Integer orderId, Integer userId);

    boolean completeOrder(Integer orderId, Integer adminId);

    boolean returnEquipment(Integer orderId, Integer adminId);
}
