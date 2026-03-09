package com.example.entity.vo.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EquipmentAddVO {
    @NotBlank(message = "设备名称不能为空")
    private String name;
    
    @NotBlank(message = "设备分类不能为空")
    private String category;
    
    private String brand;
    private String model;
    private String description;
    
    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.01", message = "日租金必须大于0")
    private BigDecimal dailyPrice;
    
    @NotNull(message = "押金不能为空")
    @DecimalMin(value = "0", message = "押金不能为负数")
    private BigDecimal deposit;
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量至少为1")
    private Integer stock;
    
    private String image;
    @NotNull(message = "状态不能为空")
    private Integer status;
}
