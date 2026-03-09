package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryVO {
    private Integer id;
    private String deliveryNo;
    private String orderNo;
    private Integer deliveryType;
    private String deliveryTypeText;
    private Integer deliveryStatus;
    private String deliveryStatusText;
    private Integer courierId;
    private String courierName;
    private String courierPhone;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private BigDecimal deliveryFee;
    private BigDecimal distance;
    private Integer estimatedTime;
    private Integer actualTime;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String remark;
    private LocalDateTime createTime;
}
