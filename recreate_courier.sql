USE test;

DROP TABLE IF EXISTS db_courier;
CREATE TABLE db_courier (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT '配送员ID',
  courier_no VARCHAR(50) NOT NULL COMMENT '配送员编号',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  phone VARCHAR(20) NOT NULL COMMENT '电话',
  id_card VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
  vehicle_type VARCHAR(50) DEFAULT NULL COMMENT '车辆类型',
  vehicle_plate VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  total_deliveries INT NOT NULL DEFAULT 0 COMMENT '总配送次数',
  total_distance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总配送里程',
  rating DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '评分',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY unique_courier_no (courier_no),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO db_courier (courier_no, name, phone, vehicle_type, vehicle_plate, status) VALUES
('CR001', '张三', '13800138010', 'ebike', '京A12345', 1),
('CR002', '李四', '13800138011', 'motorcycle', '京B23456', 1),
('CR003', '王五', '13800138012', 'van', '京C34567', 1);
