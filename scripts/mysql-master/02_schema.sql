-- 合并业务表结构（主库完整初始化）
USE `seckill_user`;
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(100) NOT NULL,
  `email` VARCHAR(100),
  `phone` VARCHAR(20),
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@seckill.com', '13800138000', 1),
('test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@seckill.com', '13800138001', 1);

USE `seckill_product`;
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL,
  `category_id` BIGINT,
  `image_url` VARCHAR(500),
  `status` TINYINT DEFAULT 1,
  `is_seckill` TINYINT DEFAULT 0,
  `seckill_price` DECIMAL(10,2),
  `seckill_start_time` DATETIME,
  `seckill_end_time` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `parent_id` BIGINT DEFAULT 0,
  `sort` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `product_category` (`name`, `parent_id`, `sort`) VALUES
('电子产品', 0, 1),
('服装鞋帽', 0, 2),
('食品饮料', 0, 3);

INSERT INTO `product` (`name`, `description`, `price`, `category_id`, `status`, `is_seckill`, `seckill_price`, `seckill_start_time`, `seckill_end_time`) VALUES
('iPhone 15 Pro', '苹果最新旗舰手机', 7999.00, 1, 1, 1, 6999.00, '2026-03-08 10:00:00', '2026-03-08 12:00:00'),
('小米13', '小米旗舰手机', 3999.00, 1, 1, 0, NULL, NULL, NULL),
('Nike运动鞋', '舒适运动鞋', 599.00, 2, 1, 1, 399.00, '2026-03-08 14:00:00', '2026-03-08 16:00:00');
