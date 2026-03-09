package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Order;
import com.example.entity.vo.request.OrderQueryVO;
import com.example.entity.vo.response.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    IPage<OrderVO> selectOrderPage(Page<?> page, @Param("userId") Integer userId, @Param("query") OrderQueryVO query);

    OrderVO selectOrderById(Integer id);

    OrderVO selectOrderByNo(@Param("orderNo") String orderNo);

    void updateDeliveryNo(@Param("orderId") Integer orderId, @Param("deliveryNo") String deliveryNo);

    void updateDepositDeductionStatus(@Param("orderId") Integer orderId, @Param("status") String status, @Param("amount") java.math.BigDecimal amount);
}
