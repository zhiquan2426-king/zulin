-- 修复 db_delivery 表结构，添加缺失的字段
USE test;

-- 添加联系人相关字段（如果不存在）
ALTER TABLE db_delivery
ADD COLUMN IF NOT EXISTS contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名' AFTER courier_phone,
ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系人电话' AFTER contact_name,
ADD COLUMN IF NOT EXISTS pickup_address VARCHAR(255) DEFAULT NULL COMMENT '取件地址' AFTER contact_phone;

-- 修改 recipient_name 和 recipient_phone 允许为空（因为某些配送类型可能不需要）
ALTER TABLE db_delivery
MODIFY COLUMN recipient_name VARCHAR(50) DEFAULT NULL COMMENT '收件人姓名',
MODIFY COLUMN recipient_phone VARCHAR(20) DEFAULT NULL COMMENT '收件人电话';

-- 添加 delivery_fee 字段（如果不存在）
ALTER TABLE db_delivery
ADD COLUMN IF NOT EXISTS delivery_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费用' AFTER pickup_address;
