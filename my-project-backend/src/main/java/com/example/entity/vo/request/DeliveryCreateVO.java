package com.example.entity.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryCreateVO {
    @NotNull(message = "订单编号不能为空")
    private String orderNo;

    @NotNull(message = "配送类型不能为空")
    private Integer deliveryType;  // 1-自提，2-配送到家，3-上门取件

    private String recipientName;

    private String recipientPhone;

    private String recipientAddress;

    private String contactName;

    private String contactPhone;

    private String pickupAddress;
}
