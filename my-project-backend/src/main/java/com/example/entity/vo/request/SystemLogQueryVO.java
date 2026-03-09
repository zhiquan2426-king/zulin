package com.example.entity.vo.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemLogQueryVO {
    private String username;
    private String operation;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 10;
}
