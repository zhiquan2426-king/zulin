package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_backup")
public class Backup {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String backupName;
    private String backupPath;
    private Long backupSize;
    private String backupType;  // full-全量，increment-增量
    private String status;  // success-成功，failed-失败
    private String description;
    private LocalDateTime createTime;
    private Integer creatorId;
}
