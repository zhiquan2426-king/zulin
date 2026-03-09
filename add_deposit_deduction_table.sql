-- 添加押金扣除机制相关表

USE `test`;

-- 创建押金扣除记录表
DROP TABLE IF EXISTS `db_deposit_deduction`;
CREATE TABLE `db_deposit_deduction` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '扣除记录ID，主键，自增',
  `order_id` int NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `user_id` int NOT NULL COMMENT '用户ID',
  `equipment_id` int NOT NULL COMMENT '设备ID',
  `deduction_type` varchar(50) NOT NULL COMMENT '扣除类型：damage-设备损坏，overdue-逾期归还',
  `deduction_amount` decimal(10,2) NOT NULL COMMENT '扣除金额',
  `deduction_reason` text COMMENT '扣除原因说明',
  `damage_images` varchar(1000) DEFAULT NULL COMMENT '损坏图片，多个用逗号分隔',
  `damage_level` varchar(20) DEFAULT NULL COMMENT '损坏程度：light-轻微，moderate-中度，severe-严重',
  `overdue_days` int DEFAULT NULL COMMENT '逾期天数',
  `status` varchar(50) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待处理，approved-已批准，rejected-已拒绝',
  `admin_id` int NOT NULL COMMENT '操作管理员ID',
  `admin_name` varchar(255) DEFAULT NULL COMMENT '操作管理员姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='押金扣除记录表';

-- 更新订单表，添加押金扣除状态字段
ALTER TABLE `db_order` 
ADD COLUMN `deposit_deduction_status` varchar(50) DEFAULT 'none' COMMENT '押金扣除状态：none-无扣除，pending-待扣除，deducted-已扣除，rejected-已拒绝' AFTER `payment_status`,
ADD COLUMN `deducted_amount` decimal(10,2) DEFAULT 0.00 COMMENT '已扣除押金金额' AFTER `deposit_deduction_status`;

-- 更新系统设置，添加押金扣除规则
INSERT INTO `db_system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('overdue_daily_penalty_rate', '0.1', '逾期每日扣除押金比例（10%）'),
('light_damage_deduction_rate', '0.2', '轻微损坏扣除押金比例（20%）'),
('moderate_damage_deduction_rate', '0.5', '中度损坏扣除押金比例（50%）'),
('severe_damage_deduction_rate', '1.0', '严重损坏扣除押金比例（100%）');

-- 添加索引
ALTER TABLE `db_order` ADD INDEX `idx_deposit_deduction_status` (`deposit_deduction_status`);
