/*
 Navicat MySQL Data Transfer

 Source Server         : 本地测试环境
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 07/08/2023 00:03:19

 数据库说明：
 - 数据库名：test
 - 字符集：utf8mb4
 - 排序规则：utf8mb4_general_ci

 注意事项：
 1. 执行前请确保 MySQL 服务已启动
 2. 请确保 MySQL 版本 >= 8.0
 3. 数据库用户需要有创建数据库和表的权限
*/

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `test`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for db_account
-- ----------------------------
DROP TABLE IF EXISTS `db_account`;
CREATE TABLE `db_account` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键，自增',
  `username` varchar(255) NOT NULL COMMENT '用户名，唯一',
  `email` varchar(255) NOT NULL COMMENT '邮箱，唯一',
  `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
  `role` varchar(255) NOT NULL DEFAULT 'user' COMMENT '用户角色（user/device_admin/system_admin）',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_email` (`email`),
  UNIQUE KEY `unique_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户账户表';

-- ----------------------------
-- Table structure for db_equipment
-- ----------------------------
DROP TABLE IF EXISTS `db_equipment`;
CREATE TABLE `db_equipment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '设备ID，主键，自增',
  `name` varchar(255) NOT NULL COMMENT '设备名称',
  `category` varchar(100) NOT NULL COMMENT '设备分类',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `description` text COMMENT '设备描述',
  `daily_price` decimal(10,2) NOT NULL COMMENT '日租金',
  `deposit` decimal(10,2) NOT NULL COMMENT '押金',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `available` int NOT NULL DEFAULT 0 COMMENT '可租数量',
  `image` varchar(255) DEFAULT NULL COMMENT '设备图片URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已下架，1-上架中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `admin_id` int DEFAULT NULL COMMENT '最后更新的设备管理员ID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备信息表';

-- ----------------------------
-- Table structure for db_order
-- ----------------------------
DROP TABLE IF EXISTS `db_order`;
CREATE TABLE `db_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键，自增',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号，唯一',
  `user_id` int NOT NULL COMMENT '用户ID',
  `equipment_id` int NOT NULL COMMENT '设备ID',
  `quantity` int NOT NULL COMMENT '租赁数量',
  `start_date` date NOT NULL COMMENT '租赁开始日期',
  `end_date` date NOT NULL COMMENT '租赁结束日期',
  `rental_days` int NOT NULL COMMENT '租赁天数',
  `daily_price` decimal(10,2) NOT NULL COMMENT '日租金',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额（租金）',
  `deposit` decimal(10,2) NOT NULL COMMENT '押金',
  `payable_amount` decimal(10,2) NOT NULL COMMENT '应付金额（租金+押金）',
  `status` varchar(50) NOT NULL DEFAULT 'pending' COMMENT '订单状态：pending-待支付，paid-已支付，renting-租赁中，completed-已完成，cancelled-已取消，overdue-已逾期',
  `payment_status` varchar(50) NOT NULL DEFAULT 'unpaid' COMMENT '支付状态：unpaid-未支付，paid-已支付，refunded-已退款',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `return_time` datetime DEFAULT NULL COMMENT '归还时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_start_end_date` (`start_date`, `end_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='租赁订单表';

-- ----------------------------
-- Table structure for db_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `db_evaluation`;
CREATE TABLE `db_evaluation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评价ID，主键，自增',
  `order_id` int NOT NULL COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `equipment_id` int NOT NULL COMMENT '设备ID',
  `rating` tinyint NOT NULL COMMENT '评分：1-5星',
  `content` text COMMENT '评价内容',
  `images` varchar(1000) DEFAULT NULL COMMENT '评价图片，多个用逗号分隔',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_order_id` (`order_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备评价表';

-- ----------------------------
-- Table structure for db_system_log
-- ----------------------------
DROP TABLE IF EXISTS `db_system_log`;
CREATE TABLE `db_system_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键，自增',
  `user_id` int DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(255) DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(255) NOT NULL COMMENT '操作类型',
  `method` varchar(255) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `time` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
  `error` text COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统日志表';

-- ----------------------------
-- Table structure for db_system_settings
-- ----------------------------
DROP TABLE IF EXISTS `db_system_settings`;
CREATE TABLE `db_system_settings` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '设置ID，主键，自增',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键，唯一',
  `setting_value` text COMMENT '设置值',
  `description` varchar(255) DEFAULT NULL COMMENT '设置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_setting_key` (`setting_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统设置表';

-- ----------------------------
-- Table structure for db_backup
-- ----------------------------
DROP TABLE IF EXISTS `db_backup`;
CREATE TABLE `db_backup` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '备份ID，主键，自增',
  `backup_name` varchar(255) NOT NULL COMMENT '备份名称',
  `backup_path` varchar(500) NOT NULL COMMENT '备份文件路径',
  `backup_size` bigint DEFAULT NULL COMMENT '备份文件大小（字节）',
  `backup_type` varchar(50) NOT NULL COMMENT '备份类型：full-全量，increment-增量',
  `status` varchar(50) NOT NULL COMMENT '状态：success-成功，failed-失败',
  `description` text COMMENT '备份描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator_id` int DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据备份表';

-- ----------------------------
-- Records of db_account (初始化测试数据)
-- ----------------------------
INSERT INTO `db_account` (`username`, `email`, `password`, `role`, `phone`) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'system_admin', '13800138000'),
('device_admin', 'device_admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'device_admin', '13800138001'),
('user', 'user@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user', '13800138002');
-- 密码都是 123456

-- ----------------------------
-- Records of db_equipment (初始化测试数据)
-- ----------------------------
INSERT INTO `db_equipment` (`name`, `category`, `brand`, `model`, `description`, `daily_price`, `deposit`, `stock`, `available`, `status`, `admin_id`) VALUES
('笔记本电脑 MacBook Pro', '电脑设备', 'Apple', 'M3 Pro', '14英寸，16GB内存，512GB硬盘，性能强劲', 99.00, 2000.00, 10, 10, 1, 2),
('投影仪 爱普生EB-X51', '投影设备', 'Epson', 'EB-X51', '3300流明，XGA分辨率，适合会议演示', 150.00, 1500.00, 5, 5, 1, 2),
('单反相机 佳能EOS R5', '摄影设备', 'Canon', 'EOS R5', '4500万像素，8K视频拍摄，专业级单反', 299.00, 5000.00, 3, 3, 1, 2),
('无人机 大疆Mavic 3', '航拍设备', 'DJI', 'Mavic 3', '哈苏相机，46分钟续航，专业航拍', 399.00, 6000.00, 2, 2, 1, 2),
('平板电脑 iPad Pro', '电脑设备', 'Apple', 'iPad Pro 12.9', 'M2芯片，Liquid视网膜显示屏', 79.00, 1800.00, 8, 8, 1, 2),
('摄像机 索尼FX3', '摄影设备', 'Sony', 'FX3', '4K视频录制，轻便型全画幅电影摄影机', 499.00, 8000.00, 2, 2, 1, 2);

-- ----------------------------
-- Records of db_system_settings (初始化系统设置)
-- ----------------------------
INSERT INTO `db_system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('site_name', '设备租赁系统', '网站名称'),
('site_description', '专业的设备租赁服务平台', '网站描述'),
('contact_email', 'contact@example.com', '联系邮箱'),
('contact_phone', '400-123-4567', '联系电话'),
('max_rental_days', '30', '最大租赁天数'),
('min_rental_days', '1', '最小租赁天数');

SET FOREIGN_KEY_CHECKS = 1;

-- 执行完成后，请确认：
-- 1. test 数据库已创建
-- 2. 所有表已创建
-- 3. 表结构包含所有字段和索引
-- 4. 字符集和排序规则正确
-- 5. 初始测试数据已插入
