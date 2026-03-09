package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationVO {
    private Integer id;
    private Integer orderId;
    private Integer userId;
    private String username;
    private String userAvatar;
    private Integer equipmentId;
    private String equipmentName;
    private Integer rating;
    private String content;
    private String images;
    private LocalDateTime createTime;
    private Integer status;
}
