package com.example.entity.vo.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderCreateVO {
    @NotNull(message = "设备ID不能为空")
    private Integer equipmentId;

    @NotNull(message = "租赁数量不能为空")
    @Min(value = 1, message = "租赁数量至少为1")
    private Integer quantity;

    @NotNull(message = "开始日期不能为空")
    @FutureOrPresent(message = "开始日期不能是过去")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Future(message = "结束日期必须是将来的日期")
    private LocalDate endDate;

    private String remark;

    private Integer deliveryType;

    private String recipientName;

    private String recipientPhone;

    private String recipientAddress;

    private String contactName;

    private String contactPhone;

    private String pickupAddress;
}
