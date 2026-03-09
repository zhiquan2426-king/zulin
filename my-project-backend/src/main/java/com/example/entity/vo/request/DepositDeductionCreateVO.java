package com.example.entity.vo.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositDeductionCreateVO {
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    @NotBlank(message = "扣除类型不能为空")
    private String deductionType;  // damage-设备损坏，overdue-逾期归还

    @NotNull(message = "扣除金额不能为空")
    @DecimalMin(value = "0.01", message = "扣除金额必须大于0")
    private BigDecimal deductionAmount;

    @NotBlank(message = "扣除原因不能为空")
    private String deductionReason;

    private String damageImages;

    private String damageLevel;  // light-轻微，moderate-中度，severe-严重

    private Integer overdueDays;
}
