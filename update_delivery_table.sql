-- 更新配送表，添加上门取件相关字段
ALTER TABLE `db_delivery`
    ADD COLUMN `contact_name` VARCHAR(50) COMMENT '联系人姓名（上门取件时需要）',
ADD COLUMN `contact_phone` VARCHAR(20) COMMENT '联系人电话（上门取件时需要）',
ADD COLUMN `pickup_address` VARCHAR(255) COMMENT '取件地址（上门取件时需要）';
