package com.example.entity.vo.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EvaluationAddVO {
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分至少为1")
    @Max(value = 5, message = "评分最多为5")
    private Integer rating;
    
    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容最多500字")
    private String content;
    
    private String images;
}
