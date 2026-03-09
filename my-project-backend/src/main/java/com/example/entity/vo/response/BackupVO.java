package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupVO {
    private Integer id;
    private String backupName;
    private String backupPath;
    private Long backupSize;
    private String backupType;
    private String status;
    private String description;
    private LocalDateTime createTime;
    private String creatorName;
}
