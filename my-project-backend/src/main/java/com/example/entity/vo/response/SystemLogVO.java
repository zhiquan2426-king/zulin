package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogVO {
    private Long id;
    private Integer userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private Long time;
    private String ip;
    private Integer status;
    private String error;
    private LocalDateTime createTime;
}
