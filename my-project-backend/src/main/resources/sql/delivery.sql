-- 配送服务相关表结构

-- 配送信息表
CREATE TABLE IF NOT EXISTS db_delivery (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '配送ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    delivery_no VARCHAR(50) NOT NULL UNIQUE COMMENT '配送单号',
    delivery_type TINYINT NOT NULL COMMENT '配送类型：1-自提，2-配送到家，3-上门取件',
    delivery_status TINYINT NOT NULL DEFAULT 1 COMMENT '配送状态：1-待配送，2-配送中，3-已签收，4-运输中，5-已完成，6-已取消',
    courier_id INT COMMENT '配送员ID',
    courier_name VARCHAR(50) COMMENT '配送员姓名',
    courier_phone VARCHAR(20) COMMENT '配送员电话',
    recipient_name VARCHAR(50) NOT NULL COMMENT '收件人姓名',
    recipient_phone VARCHAR(20) NOT NULL COMMENT '收件人电话',
    recipient_address VARCHAR(255) COMMENT '收件地址（配送到家时需要）',
    delivery_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费用',
    distance DECIMAL(10,2) COMMENT '配送距离（公里）',
    estimated_time INT COMMENT '预计配送时长（分钟）',
    actual_time INT COMMENT '实际配送时长（分钟）',
    start_time DATETIME COMMENT '配送开始时间',
    complete_time DATETIME COMMENT '配送完成时间',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_delivery_no (delivery_no),
    INDEX idx_courier_id (courier_id),
    INDEX idx_delivery_status (delivery_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='配送信息表';

-- 配送轨迹表
CREATE TABLE IF NOT EXISTS db_delivery_tracking (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '轨迹ID',
    delivery_no VARCHAR(50) NOT NULL COMMENT '配送单号',
    tracking_status TINYINT NOT NULL COMMENT '状态：1-待取件，2-已取件，3-运输中，4-派送中，5-已签收，6-异常',
    location VARCHAR(255) COMMENT '当前位置',
    description VARCHAR(255) COMMENT '状态描述',
    operator VARCHAR(50) COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_delivery_no (delivery_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='配送轨迹表';

-- 配送员表
CREATE TABLE IF NOT EXISTS db_courier (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '配送员ID',
    courier_no VARCHAR(50) NOT NULL UNIQUE COMMENT '配送员编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL COMMENT '电话',
    id_card VARCHAR(18) COMMENT '身份证号',
    vehicle_type VARCHAR(50) COMMENT '车辆类型：电动车，摩托车，面包车，货车',
    vehicle_plate VARCHAR(20) COMMENT '车牌号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-空闲，2-忙碌，3-休息，0-禁用',
    total_deliveries INT DEFAULT 0 COMMENT '总配送次数',
    total_distance DECIMAL(10,2) DEFAULT 0.00 COMMENT '总配送里程',
    rating DECIMAL(3,2) DEFAULT 5.00 COMMENT '评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_courier_no (courier_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='配送员表';

-- 配送费用表
CREATE TABLE IF NOT EXISTS db_delivery_fee (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '费用ID',
    delivery_type TINYINT NOT NULL COMMENT '配送类型：1-自提，2-配送到家，3-上门取件',
    min_distance DECIMAL(10,2) DEFAULT 0.00 COMMENT '最小距离',
    max_distance DECIMAL(10,2) DEFAULT 0.00 COMMENT '最大距离',
    base_fee DECIMAL(10,2) NOT NULL COMMENT '基础费用',
    unit_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '单位费用（每公里）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='配送费用表';

-- 插入默认配送费用
INSERT INTO db_delivery_fee (delivery_type, min_distance, max_distance, base_fee, unit_fee) VALUES
(1, 0, 0, 0.00, 0.00),  -- 自提：免费
(2, 0, 5, 10.00, 2.00),   -- 配送到家：5公里内10元，超出的每公里2元
(2, 5, 10, 18.00, 1.50),  -- 配送到家：5-10公里18元，超出的每公里1.5元
(2, 10, 999, 25.00, 1.00), -- 配送到家：10公里以上25元，超出的每公里1元
(3, 0, 5, 8.00, 1.50),     -- 上门取件：5公里内8元，超出的每公里1.5元
(3, 5, 10, 14.00, 1.20),    -- 上门取件：5-10公里14元，超出的每公里1.2元
(3, 10, 999, 20.00, 0.80); -- 上门取件：10公里以上20元，超出的每公里0.8元
