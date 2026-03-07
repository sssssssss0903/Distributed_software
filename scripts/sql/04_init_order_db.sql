-- 创建订单服务数据库
CREATE DATABASE IF NOT EXISTS `seckill_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `seckill_order`;

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态:0-待支付,1-已支付,2-已取消,3-已完成',
  `is_seckill` TINYINT DEFAULT 0 COMMENT '是否秒杀订单:0-否,1-是',
  `pay_time` DATETIME COMMENT '支付时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user (`user_id`),
  INDEX idx_order_no (`order_no`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单项表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_order (`order_id`),
  INDEX idx_product (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';
