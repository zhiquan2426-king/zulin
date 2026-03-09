package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Delivery;
import com.example.entity.vo.response.DeliveryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {

    @Select("<script>" +
            "SELECT d.*, " +
            "CASE d.delivery_type WHEN 1 THEN '自提' WHEN 2 THEN '配送到家' WHEN 3 THEN '上门取件' END AS delivery_type_text, " +
            "CASE d.delivery_status WHEN 1 THEN '待配送' WHEN 2 THEN '配送中' WHEN 3 THEN '已签收' " +
            "WHEN 4 THEN '运输中' WHEN 5 THEN '已完成' WHEN 6 THEN '已取消' END AS delivery_status_text " +
            "FROM db_delivery d " +
            "WHERE 1=1 " +
            "<if test='userId != null'>AND d.id IN (SELECT id FROM db_delivery WHERE order_no IN " +
            "(SELECT order_no FROM db_order WHERE user_id = #{userId}))</if> " +
            "<if test='status != null'>AND d.delivery_status = #{status}</if> " +
            "<if test='orderNo != null and orderNo != \"\"'>AND d.order_no LIKE CONCAT('%', #{orderNo}, '%')</if> " +
            "ORDER BY d.create_time DESC" +
            "</script>")
    IPage<DeliveryVO> selectDeliveryPage(Page<DeliveryVO> page,
                                          @Param("userId") Integer userId,
                                          @Param("status") Integer status,
                                          @Param("orderNo") String orderNo);

    @Select("SELECT * FROM db_delivery WHERE delivery_no = #{deliveryNo}")
    DeliveryVO selectDeliveryByNo(@Param("deliveryNo") String deliveryNo);
}
